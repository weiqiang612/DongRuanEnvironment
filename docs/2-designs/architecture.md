# 系统架构设计

## 当前实现架构

- `back`：Spring Boot REST API，端口 `8080`。
- `front`：Vue 3 单页应用；开发期将 `/api` 代理到后端。
- MySQL：由 Spring `JdbcTemplate` 和 MyBatis-Plus 访问。

控制器处理路由与请求/响应；四端登录端点集中在 `AuthController`，但 NEPS 与员工端仍调用各自认证服务和 DTO，避免混淆手机号与业务编码认证契约；Mapper 与 `JdbcTemplate` 执行数据访问；`ResultVO` 统一封装接口响应。前端 API 客户端集中在 `front/src/api/`，视图位于 `front/src/views/`。NEPS 登录查询 `supervisor`；NEPG 登录查询 `grid_member.gm_code`；NEPM、NEPV 登录查询 `admins.admin_code` 并校验 `role_code`。四端认证均复用 PBKDF2，成功后仅在 HTTP Session 写入非敏感身份和角色信息；NEPS 退出登录通过专用端点销毁当前 Session。

## 目标逻辑架构

完整系统由 NEPS、NEPG、NEPM、NEPV 四端，统一业务服务，MySQL 和东软 HR 外部接口组成。业务服务划分为认证授权、反馈管理、任务调度、AQI 检测、预警超时、统计展示六个模块；所有前端和外部集成均只能经服务层访问数据。

```text
NEPS / NEPG / NEPM / NEPV
            │ HTTPS + JSON
            ▼
认证授权 → 反馈管理 → 任务调度 → AQI 检测 → 预警超时 / 统计展示
            │              │
            ▼              ▼
          MySQL       东软 HR 接口
```

主链路为：NEPS 创建反馈 → NEPM 查询候选网格员并指派 → NEPG 提交实测数据 → 服务层计算最终 AQI → 生成预警、超时标识和统计 → NEPM、NEPV 展示结果。超时由定时扫描与查询时判定结合处理。

TASK-007 的 NEPM 调度层复用 `aqi_feedback` 作为任务主表、`grid_member` 作为候选人员来源、`admins` 作为操作人来源和 `task_assign_log` 作为追溯记录。管理端 Controller 在每个入口校验员工 Session 中的 `employeeRole = NEPM_ADMIN` 与 `employeeAccountCode`，服务层再解析实际 `admin_id`；首次指派、重派和继续处理在同一事务中校验反馈状态、候选网格员可工作性并写入日志。候选查询只使用本地 `grid_member.state = 0`，按同城优先、同省其他城市兜底，不在事务中调用东软 HR。

TASK-008 的 NEPG 任务层从员工 Session 读取 `employeeRole = NEPG_GRID_MEMBER` 与 `employeeAccountCode`，服务层解析实际 `grid_member.gm_id`，浏览器不传递网格员身份或最终事实。查询仅返回该网格员 `state in (1, 2)` 的任务；提交在同一事务内完成 AQI 字典校验、`detection_result` 插入、任务完成与等级 4 至 6 的 `alert_record` 写入。唯一索引与状态条件共同抵御重复提交，异常时不遗留半完成数据。

TASK-009 的 NEPM 检测处置与运营统计层构建在 TASK-007 与 TASK-008 产生的数据基石之上，所有入口统一校验 Session 中的 `employeeRole = NEPM_ADMIN` 与非空 `employeeAccountCode`：
1. **检测结果只读查询链路**：服务层联合 `detection_result`、`aqi_feedback`、`grid_province`、`grid_city` 及 `grid_member` 进行多表只读投影，返回完整的三项实测数据、分项等级、系统最终 AQI 等级及关联网格员与反馈信息，不提供任何写操作通道，确保检测结果的客观只读性。
2. **AQI 预警原子条件更新机制**：对于 4~6 级高等级预警（`alert_record`），查询端点支持按状态（`PENDING`/`HANDLED`）与省市过滤；处置操作执行数据库级原子条件更新（`UPDATE alert_record SET alert_status = 'HANDLED', handled_at = ? WHERE id = ? AND alert_status = 'PENDING'`），受影响行数不为 1 时视为已被其他管理员处置或状态不符，由服务层抛出状态冲突异常并映射为 HTTP 409，杜绝并发竞争与重复处置，且处置请求无需前端传递额外业务字段，处置时间严格以服务端当前时间为准。
3. **超时任务调度复用机制**：超时任务的处置与重派完全复用 TASK-007 既有的调度机制（`POST /nepm/feedbacks/{afId}/dispatch`），支持对超时任务重新指派同城或同省备选网格员，由调度事务同步更新任务归属并落库 `task_assign_log` 审计日志，保证调度链路单一真实来源与逻辑复用。
4. **综合运营统计聚合链路**：直接复用 `detection_result`、`aqi_feedback` 和 `alert_record`，以只读 SQL 聚合方式动态统计完成检测总量、1~6 级 AQI 等级分布、月度趋势、高等级预警状态汇总（总数、待处置、已处置），无需新增任何表结构或维护异步汇总表，保证口径严密且与事务数据强一致。
5. **近期运营趋势链路**：管理端首页按 `submitted_at` 统计每日新增反馈，按 `completed_at` 统计每日办结反馈，并以“截至当日结束已提交但尚未完成”的存量计算日终待处理数。三项均为真实业务事实；不得用办结数除以新增数伪造办结率，不得补造零值样本或截断统计值。


## 设计约束

- 依赖方向固定为“前端 → 服务层 → 数据访问层或外部接口”，禁止跨层直接访问。
- 浏览器端不得访问数据库或 HR 系统；HR 集成仅由服务端使用服务账号调用。
- AQI、预警、超时和状态流转规则集中在服务层；关键写操作使用事务并保留操作日志。
- 公众数据按本人归属隔离，员工端按角色授权；前端不保存或计算最终 AQI。
- 当前四端登录不引入全局拦截器或 JWT；NEPS 反馈 Controller 在每个反馈入口读取 Session 中的 `nepsSupervisorTelId`，服务层将该身份作为查询和写操作的归属边界。保存时归属由服务端写入，列表和详情按归属过滤，更新和删除以“本人、待指派且未超时”为原子条件执行；后续员工业务页面授权仍在独立任务中实施。

## 当前接口边界

前端请求路径必须带 `/api`，后端控制器不带该前缀。后端监听 `8080`；Swagger UI 为 `/swagger-ui.html`，OpenAPI 文档为 `/api-docs`。目标接口的认证、传输和状态规则见 `api_contract.md`，尚未实现的模块不可据此直接调用。
