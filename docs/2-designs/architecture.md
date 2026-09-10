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

## 设计约束

- 依赖方向固定为“前端 → 服务层 → 数据访问层或外部接口”，禁止跨层直接访问。
- 浏览器端不得访问数据库或 HR 系统；HR 集成仅由服务端使用服务账号调用。
- AQI、预警、超时和状态流转规则集中在服务层；关键写操作使用事务并保留操作日志。
- 公众数据按本人归属隔离，员工端按角色授权；前端不保存或计算最终 AQI。
- 当前四端登录不引入全局拦截器或 JWT；NEPS 反馈 Controller 在每个反馈入口读取 Session 中的 `nepsSupervisorTelId`，服务层将该身份作为查询和写操作的归属边界。保存时归属由服务端写入，列表和详情按归属过滤，更新和删除以“本人、待指派且未超时”为原子条件执行；后续员工业务页面授权仍在独立任务中实施。

## 当前接口边界

前端请求路径必须带 `/api`，后端控制器不带该前缀。后端监听 `8080`；Swagger UI 为 `/swagger-ui.html`，OpenAPI 文档为 `/api-docs`。目标接口的认证、传输和状态规则见 `api_contract.md`，尚未实现的模块不可据此直接调用。
