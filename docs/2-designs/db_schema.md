# 数据库设计

## 当前基线

本地 `nepmdb` 由历史 `nepmdb.sql` 初始化，当前业务表为 `admins`、`aqi`、`aqi_feedback`、`grid_province`、`grid_city`、`grid_member`、`statistics`、`supervisor`。`省市.sql` 中的 `province`、`city` 是另一套 GBK 编码的重复地区表，当前代码未使用，不纳入迁移。

| 现有表 | 保留策略 | 说明 |
|---|---|---|
| `supervisor` | 扩展 | 公众监督员身份来源，`tel_id` 是反馈归属标识；`password` 扩容为 PBKDF2 哈希存储，本次不新增 `public_user`。 |
| `grid_province`、`grid_city`、`grid_member` | 保留 | 当前网格与网格员基础数据；不新增 `grid_region`。 |
| `aqi` | 保留 | 保存 AQI 六级、颜色及污染物范围；计算实现前需明确相邻等级边界。 |
| `aqi_feedback` | 扩展 | 同时承担公众反馈和任务主状态，不新增 `inspection_task`。 |
| `statistics` | 重构为 `detection_result` | 原表是单次确认检测明细，当前为空表，不是汇总统计表。 |
| `task_assign_log` | 新增 | 保存首次指派、重派和继续处理。 |
| `alert_record` | 新增 | 仅保存最终 AQI 达到四级及以上的高等级预警及处置。 |

## 反馈与任务状态

`aqi_feedback` 的主状态统一为 `0` 待指派、`1` 已指派、`2` 已完成；`已超时` 是独立的高优先级展示标识，不替代主状态，也不阻断管理员重派或继续处理。超时规则为待指派超过 2 小时，或已指派超过 24 小时仍未完成。

保留原 `af_date`、`af_time`、`assign_date`、`assign_time` 以兼容当前接口；新增并回填以下规范时间字段：

| 字段 | 用途 |
|---|---|
| `submitted_at` | 提交时间，用于本人历史列表和待指派超时判定；旧接口未传该字段时默认取当前时间。 |
| `assigned_at` | 最近一次指派时间，用于已指派超时判定。 |
| `completed_at` | 任务完成时间。 |
| `updated_at` | 记录最近更新时间。 |
| `timeout_flag`、`timeout_at` | 当前是否超时及首次标记超时的时间。 |

`gm_id` 统一为与 `grid_member.gm_id` 一致的 `VARCHAR(11)`。为公众历史查询和管理员超时查询建立 `tel_id + submitted_at`、`state + timeout_flag + assigned_at` 索引。

当前 Java 映射中，`af_date`、`af_time` 与 `assign_date`、`assign_time` 保持字符串以兼容既有接口；`submitted_at`、`assigned_at`、`completed_at`、`updated_at`、`timeout_at` 映射为 `LocalDateTime`，`gm_id` 映射为 `String`，`state` 映射为 `Integer`，`timeout_flag` 映射为 `Boolean`。这些字段由服务层和数据库默认值维护，不作为反馈保存/更新请求的可写字段。

## 公众监督员登录

`supervisor.tel_id` 是 NEPS 手机号登录标识。迁移将历史 `password VARCHAR(20)` 扩容为 `VARCHAR(256)`，用于存储 PBKDF2 格式的密码哈希；不新增明文密码列。为兼容历史账号，认证服务仅在首次成功验证历史值时将其立即原位改写为 PBKDF2 哈希；后续登录仅比较哈希。密码、哈希均不得在接口响应、日志或前端状态中出现。

## 逻辑外键策略

本项目不使用 MySQL `FOREIGN KEY` 物理约束，所有跨表关联均采用逻辑外键：保留语义明确的关联字段和查询索引，由服务层在新增、修改和删除前校验关联对象是否存在，并显式处理删除限制或历史留痕。数据库不使用级联删除或更新。

| 关联字段 | 逻辑关联对象 | 保留方式 |
|---|---|---|
| `aqi_feedback.province_id`、`city_id` | `grid_province`、`grid_city` | 历史索引保留，服务层校验地区归属。 |
| `aqi_feedback.gm_id` | `grid_member.gm_id` | 字符串标识保留，服务层校验指派对象。 |
| `grid_city.province_id`、`grid_member.province_id`、`grid_member.city_id` | 网格省市表 | 历史索引保留，服务层校验层级关系。 |
| `detection_result.feedback_id` | `aqi_feedback.af_id` | 唯一索引保证一条反馈最多一条结果；服务层校验反馈存在。 |
| `task_assign_log.feedback_id`、`operator_id` | `aqi_feedback`、`admins` | `feedback_id + created_at` 索引用于追溯；服务层校验操作主体。 |
| `alert_record.feedback_id`、`result_id` | `aqi_feedback`、`detection_result` | `result_id` 唯一索引保证一条结果最多一条预警；服务层校验关联一致性。 |

## 检测结果与统计

历史 `statistics` 更名为 `detection_result`，并以 `feedback_id` 唯一关联 `aqi_feedback.af_id`。一条反馈最多有一条最终检测结果，表中保存网格员、SO₂/CO/PM2.5 实测值及等级、最终 AQI 等级和检测时间。

省、市、地址、反馈人和反馈描述不在检测结果表重复保存，统一通过 `feedback_id` 关联反馈表获取。省分组、AQI 分布、趋势和检测数量等统计，直接基于 `detection_result` 与 `aqi_feedback` 聚合，不再建立重复汇总表。

## 指派日志与预警

`task_assign_log` 以 `feedback_id` 关联反馈，记录管理员、原/新网格员、`ASSIGN`、`REASSIGN`、`CONTINUE` 操作和操作时间。`alert_record` 以 `feedback_id` 与 `result_id` 关联，保存预警 AQI 等级、处置状态、生成时间和处置时间；只有最终 AQI 大于等于 4 时创建。超时不创建独立预警记录。

## 迁移与回滚

迁移脚本位于 `back/db/migration/`。先检查 `statistics` 必须为空、反馈状态仅为 0 至 2、日期时间可解析，再执行表重命名、字段补充和新表创建。`V20260908_002` 负责移除历史及早期迁移遗留的全部物理外键，并修复因非 UTF-8 客户端写入的中文注释；`V20260908_003` 为旧接口补充 `submitted_at` 的当前时间默认值。执行 SQL 时必须使用 UTF-8 客户端（`mysql --default-character-set=utf8mb4`）。MySQL DDL 会隐式提交，因此执行前须导出受影响表结构和数据；回滚脚本仅用于恢复本次新增结构和空表重命名，不能替代数据备份。

数据库凭据仅从本地 `back/src/main/resources/application.yaml` 读取，不得复制到脚本、文档、日志或提交内容。新增或变更表、字段、索引、约束、状态语义或数据迁移前，更新本文件并先确认迁移与回滚方案。
