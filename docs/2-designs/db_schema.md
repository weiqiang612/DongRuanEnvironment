# 数据库设计

## 当前基线

本地 `nepmdb` 由历史 `nepmdb.sql` 初始化，当前业务表为 `admins`、`aqi`、`aqi_feedback`、`grid_province`、`grid_city`、`grid_member`、`statistics`、`supervisor`。`省市.sql` 中的 `province`、`city` 是另一套 GBK 编码的重复地区表，当前代码未使用，不纳入迁移。

| 现有表 | 保留策略 | 说明 |
|---|---|---|
| `supervisor` | 扩展 | 公众监督员身份来源，`tel_id` 是反馈归属标识；`password` 扩容为 PBKDF2 哈希存储，本次不新增 `public_user`。 |
| `grid_province`、`grid_city`、`grid_member` | 保留 | 当前网格与网格员基础数据；`gm_code` 用于 NEPG 登录，`password` 扩容为 PBKDF2 哈希存储。 |
| `admins` | 扩展 | `admin_code` 用于 NEPM、NEPV 登录；`password` 扩容为 PBKDF2 哈希存储，`role_code` 明确员工端角色。 |
| `aqi` | 保留 | 保存 AQI 六级、颜色及污染物范围；计算实现前需明确相邻等级边界。 |
| `aqi_feedback` | 扩展 | 同时承担公众反馈和任务主状态，不新增 `inspection_task`。 |
| `statistics` | 重构为 `detection_result` | 原表是单次确认检测明细，当前为空表，不是汇总统计表。 |
| `task_assign_log` | 新增 | 保存首次指派、重派和继续处理。 |
| `alert_record` | 新增 | 仅保存最终 AQI 达到四级及以上的高等级预警及处置。 |

## 实际数据库数据字典（2026-09-09 同步）

本节通过 `information_schema` 从本地 `nepmdb` 的迁移后实例读取，是字段类型、默认值、索引和物理约束的查阅基准；不记录任何业务行、账号或密码值。除 `aqi`、`aqi_feedback`、`detection_result`、`grid_member` 使用 `utf8mb4_latvian_ci` 外，其他表使用 `utf8mb4_0900_ai_ci`；全部为 InnoDB 基表。当前库没有物理外键，名称中以 `fk_` 开头的索引仅用于关联查询。

| 表 | 表说明 |
|---|---|
| `admins` | 系统管理员与 NEPM/NEPV 身份来源。 |
| `alert_record` | 高等级 AQI 预警记录。 |
| `aqi` | 六级 AQI 与污染物区间基础字典。 |
| `aqi_feedback` | 公众反馈及检测任务主表。 |
| `detection_result` | 一次反馈对应的最终检测结果。 |
| `grid_city`、`grid_province` | 系统网格省市基础字典。 |
| `grid_member` | 网格员身份与所属网格。 |
| `supervisor` | NEPS 公众监督员身份来源。 |
| `task_assign_log` | 反馈指派、重派与继续处理日志。 |

字段表中的“键”含义：`PK` 主键、`UQ` 唯一索引、`IDX` 普通索引；`—` 表示无索引。`NULL` 表示默认值为 SQL `NULL`，不是文本值。

### `admins`

| 字段 | 类型 | 可空 | 默认值 | 键 | 说明 |
|---|---|---:|---|---|---|
| `admin_id` | `INT` | 否 | — | PK，自增 | 系统管理员编号。 |
| `admin_code` | `VARCHAR(20)` | 否 | — | UQ `uk_admins_admin_code` | 系统管理员登录编号。 |
| `password` | `VARCHAR(256)` | 否 | — | — | PBKDF2 密码哈希或待首次成功登录升级的历史密码。 |
| `remarks` | `VARCHAR(100)` | 是 | `NULL` | — | 备注。 |
| `role_code` | `VARCHAR(32)` | 是 | `NULL` | — | `NEPM_ADMIN` 或 `NEPV_DECISION_MAKER`。 |

### `alert_record`

表注释：高等级 AQI 预警记录。

| 字段 | 类型 | 可空 | 默认值 | 键 | 说明 |
|---|---|---:|---|---|---|
| `id` | `BIGINT` | 否 | — | PK，自增 | 预警记录主键。 |
| `feedback_id` | `INT` | 否 | — | IDX `fk_alert_record_feedback` | 关联 `aqi_feedback.af_id`。 |
| `result_id` | `INT` | 否 | — | UQ `uk_alert_record_result` | 关联 `detection_result.id`。 |
| `alert_level` | `TINYINT UNSIGNED` | 否 | — | — | 预警 AQI 等级，取值 4 至 6。 |
| `alert_status` | `VARCHAR(20)` | 否 | `PENDING` | IDX（与 `created_at` 组合） | `PENDING`、`HANDLED`。 |
| `created_at` | `DATETIME` | 否 | `CURRENT_TIMESTAMP` | IDX（与 `alert_status` 组合） | 生成时间。 |
| `handled_at` | `DATETIME` | 是 | `NULL` | — | 处置时间。 |

### `aqi`

| 字段 | 类型 | 可空 | 默认值 | 键 | 说明 |
|---|---|---:|---|---|---|
| `aqi_id` | `INT` | 否 | — | PK，自增 | 空气质量指数级别，共六级。 |
| `chinese_explain` | `VARCHAR(10)` | 否 | — | — | AQI 等级汉字表述。 |
| `aqi_explain` | `VARCHAR(20)` | 否 | — | — | AQI 等级描述。 |
| `color` | `VARCHAR(7)` | 否 | — | — | AQI 等级颜色。 |
| `health_impact` | `VARCHAR(500)` | 否 | — | — | 对健康影响情况。 |
| `take_steps` | `VARCHAR(500)` | 否 | — | — | 建议采取的措施。 |
| `so2_min` | `INT` | 否 | — | — | 本级 SO₂ 浓度最小限值。 |
| `so2_max` | `INT` | 否 | — | — | 本级 SO₂ 浓度最大限值。 |
| `co_min` | `INT` | 否 | — | — | 本级 CO 浓度最小限值。 |
| `co_max` | `INT` | 否 | — | — | 本级 CO 浓度最大限值。 |
| `spm_min` | `INT` | 否 | — | — | 本级悬浮颗粒物浓度最小限值。 |
| `spm_max` | `INT` | 否 | — | — | 本级悬浮颗粒物浓度最大限值。 |
| `remarks` | `VARCHAR(100)` | 是 | `NULL` | — | 备注。 |

### `aqi_feedback`

| 字段 | 类型 | 可空 | 默认值 | 键 | 说明 |
|---|---|---:|---|---|---|
| `af_id` | `INT` | 否 | — | PK，自增 | 空气质量公众监督反馈编号。 |
| `tel_id` | `VARCHAR(20)` | 否 | — | IDX（与 `submitted_at` 组合） | 所属公众监督员编号，即手机号。 |
| `province_id` | `INT` | 否 | — | IDX `fk_api_province_id` | 反馈所在省区域编号。 |
| `city_id` | `INT` | 否 | — | IDX `fk_api_city_id` | 反馈所在市区域编号。 |
| `address` | `VARCHAR(200)` | 否 | — | — | 反馈详细地址。 |
| `information` | `VARCHAR(400)` | 否 | — | — | 反馈描述。 |
| `estimated_grade` | `INT` | 否 | — | — | 反馈者预估 AQI 等级。 |
| `af_date` | `VARCHAR(20)` | 否 | — | — | 历史兼容反馈日期。 |
| `af_time` | `VARCHAR(20)` | 否 | — | — | 历史兼容反馈时间。 |
| `submitted_at` | `DATETIME` | 否 | `CURRENT_TIMESTAMP` | IDX（与 `tel_id` 组合） | 反馈提交时间。 |
| `gm_id` | `VARCHAR(11)` | 是 | `NULL` | — | 指派网格员编号。 |
| `assign_date` | `VARCHAR(20)` | 是 | `NULL` | — | 历史兼容指派日期。 |
| `assign_time` | `VARCHAR(20)` | 是 | `NULL` | — | 历史兼容指派时间。 |
| `assigned_at` | `DATETIME` | 是 | `NULL` | IDX（与 `state`、`timeout_flag` 组合） | 最近一次指派时间。 |
| `completed_at` | `DATETIME` | 是 | `NULL` | — | 任务完成时间。 |
| `updated_at` | `DATETIME` | 否 | `CURRENT_TIMESTAMP`，更新时自动刷新 | — | 更新时间。 |
| `timeout_flag` | `TINYINT(1)` | 否 | `0` | IDX（与 `state`、`assigned_at` 组合） | 是否超时：`0` 否、`1` 是。 |
| `timeout_at` | `DATETIME` | 是 | `NULL` | — | 首次标记超时时间。 |
| `state` | `TINYINT UNSIGNED` | 否 | `0` | IDX（与 `timeout_flag`、`assigned_at` 组合） | `0` 待指派、`1` 已指派、`2` 已完成。 |
| `remarks` | `VARCHAR(200)` | 是 | `NULL` | — | 备注。 |

### `detection_result`

| 字段 | 类型 | 可空 | 默认值 | 键 | 说明 |
|---|---|---:|---|---|---|
| `id` | `INT` | 否 | — | PK，自增 | 检测结果编号。 |
| `feedback_id` | `INT` | 否 | — | UQ `uk_detection_result_feedback` | 关联 `aqi_feedback.af_id`，一条反馈至多一条结果。 |
| `so2_value` | `DECIMAL(10,2)` | 否 | — | — | SO₂ 实测浓度。 |
| `so2_level` | `INT` | 否 | — | — | SO₂ 指数级别。 |
| `co_value` | `DECIMAL(10,2)` | 否 | — | — | CO 实测浓度。 |
| `co_level` | `INT` | 否 | — | — | CO 指数级别。 |
| `spm_value` | `DECIMAL(10,2)` | 否 | — | — | PM2.5 实测浓度。 |
| `spm_level` | `INT` | 否 | — | — | PM2.5 指数级别。 |
| `aqi_id` | `INT` | 否 | — | IDX（与 `detected_at` 组合） | 最终 AQI 等级。 |
| `detected_at` | `DATETIME` | 否 | — | IDX（与 `aqi_id` 组合） | 检测时间。 |
| `gm_id` | `VARCHAR(11)` | 否 | — | — | 检测网格员编号。 |

### `grid_province` 与 `grid_city`

| 表 | 字段 | 类型 | 可空 | 默认值 | 键 | 说明 |
|---|---|---|---:|---|---|---|
| `grid_province` | `province_id` | `INT` | 否 | — | PK，自增 | 系统网格覆盖省区域编号。 |
| `grid_province` | `province_name` | `VARCHAR(100)` | 否 | — | — | 系统网格覆盖省区域名称。 |
| `grid_province` | `province_abbr` | `VARCHAR(50)` | 否 | — | — | 系统网格覆盖省区域简称。 |
| `grid_province` | `remarks` | `VARCHAR(200)` | 是 | `NULL` | — | 备注。 |
| `grid_city` | `city_id` | `INT` | 否 | — | PK，自增 | 系统网格覆盖市区域编号。 |
| `grid_city` | `city_name` | `VARCHAR(100)` | 否 | — | — | 系统网格覆盖市区域名称。 |
| `grid_city` | `province_id` | `INT` | 否 | — | IDX `fk_city_province_id` | 所属省区域编号。 |
| `grid_city` | `remarks` | `VARCHAR(200)` | 是 | `NULL` | — | 备注。 |

直辖市在 `grid_city` 中同样必须至少有一条与省级名称相同的城市选项，以保证省市级联选择可用。`V20260910_006` 为缺失选项的上海市、重庆市补充该基础数据；迁移按省份无城市记录的条件执行，重复运行不会重复插入。

### `grid_member`

| 字段 | 类型 | 可空 | 默认值 | 键 | 说明 |
|---|---|---:|---|---|---|
| `gm_id` | `VARCHAR(11)` | 否 | — | PK | 网格员手机号码。 |
| `gm_name` | `VARCHAR(20)` | 否 | — | — | 网格员名称。 |
| `gm_code` | `VARCHAR(20)` | 否 | — | UQ `uk_grid_member_gm_code` | NEPG 登录编码。 |
| `password` | `VARCHAR(256)` | 否 | — | — | PBKDF2 密码哈希或待首次成功认证升级的历史密码。 |
| `province_id` | `INT` | 否 | — | IDX `fk_province_id` | 网格所属省编号。 |
| `city_id` | `INT` | 否 | — | IDX `fk_city_id` | 网格所属市编号。 |
| `tel` | `VARCHAR(20)` | 否 | — | — | 联系电话。 |
| `state` | `INT` | 否 | `0` | — | `0` 可工作、`1` 临时抽调、`2` 休假、`3` 其他。 |
| `remarks` | `VARCHAR(200)` | 是 | `NULL` | — | 备注。 |

### `supervisor`

| 字段 | 类型 | 可空 | 默认值 | 键 | 说明 |
|---|---|---:|---|---|---|
| `tel_id` | `VARCHAR(11)` | 否 | — | PK | 公众监督员编号，即手机号；NEPS 登录标识。 |
| `password` | `VARCHAR(256)` | 否 | — | — | PBKDF2 密码哈希；历史明文首次登录成功后升级。 |
| `real_name` | `VARCHAR(20)` | 否 | — | — | 公众监督员真实姓名。 |
| `birthday` | `VARCHAR(20)` | 否 | — | — | 出生日期。 |
| `sex` | `INT` | 否 | `1` | — | `1` 男、`0` 女。 |
| `remarks` | `VARCHAR(200)` | 是 | `NULL` | — | 备注。 |

### `task_assign_log`

表注释：反馈指派操作日志。

| 字段 | 类型 | 可空 | 默认值 | 键 | 说明 |
|---|---|---:|---|---|---|
| `id` | `BIGINT` | 否 | — | PK，自增 | 指派日志主键。 |
| `feedback_id` | `INT` | 否 | — | IDX（与 `created_at` 组合） | 关联 `aqi_feedback.af_id`。 |
| `operator_id` | `INT` | 否 | — | IDX `fk_task_assign_log_admin` | 操作管理员编号。 |
| `from_gm_id` | `VARCHAR(11)` | 是 | `NULL` | — | 原网格员编号。 |
| `to_gm_id` | `VARCHAR(11)` | 是 | `NULL` | — | 新网格员编号。 |
| `action_type` | `VARCHAR(20)` | 否 | — | — | `ASSIGN`、`REASSIGN`、`CONTINUE`。 |
| `created_at` | `DATETIME` | 否 | `CURRENT_TIMESTAMP` | IDX（与 `feedback_id` 组合） | 操作时间。 |

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

`supervisor.tel_id` 是 NEPS 手机号登录标识。迁移将历史 `password VARCHAR(20)` 扩容为 `VARCHAR(256)`，用于存储 `pbkdf2$600000$盐$哈希` 格式的 PBKDF2-HMAC-SHA256 密码哈希；不新增明文密码列。为兼容历史账号，认证服务仅在首次成功验证历史值时将其立即原位改写为 PBKDF2 哈希；后续登录仅比较哈希。密码、哈希均不得在接口响应、日志或前端状态中出现。

## 员工端登录

`grid_member.gm_code` 是 NEPG 登录标识，`admins.admin_code` 是 NEPM、NEPV 登录标识。两字段由 `V20260909_005` 增加唯一索引，确保每个业务编号只能对应一个登录主体。两表既有 `password VARCHAR(20)` 均由该迁移扩容为 `VARCHAR(256)`，以保存与 NEPS 一致的 PBKDF2-HMAC-SHA256 格式；首次成功认证历史明文时原位升级为哈希。

`admins.role_code VARCHAR(32)` 可为空但不允许以空值登录，允许值仅为 `NEPM_ADMIN` 和 `NEPV_DECISION_MAKER`。迁移不创建账号、密码或角色，账号维护人员必须显式配置角色。由于 PBKDF2 哈希不能安全缩回 `VARCHAR(20)`，回滚前必须恢复迁移前备份，回滚脚本只移除 `role_code`，不自动截断密码字段。

`role_code` 为空、值不属于上述枚举，或角色与目标端不匹配时，认证服务统一按认证失败处理，不返回角色存在性或配置细节。数据库层暂不增加 `CHECK` 约束，以保持历史 MySQL 部署兼容；角色合法性由服务层固定枚举控制。

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

迁移脚本位于 `back/db/migration/`。先检查 `statistics` 必须为空、反馈状态仅为 0 至 2、日期时间可解析，再执行表重命名、字段补充和新表创建。`V20260908_002` 负责移除历史及早期迁移遗留的全部物理外键，并修复因非 UTF-8 客户端写入的中文注释；`V20260908_003` 为旧接口补充 `submitted_at` 的当前时间默认值；`V20260909_005` 扩容员工端密码并增加管理员角色；`V20260910_006` 为上海市、重庆市补齐省市级联所需的同名城市基础数据。执行 SQL 时必须使用 UTF-8 客户端（`mysql --default-character-set=utf8mb4`）。MySQL DDL 会隐式提交，因此执行前须导出受影响表结构和数据；回滚脚本仅用于恢复本次新增结构和空表重命名，不能替代数据备份。

数据库凭据仅从本地 `back/src/main/resources/application.yaml` 读取，不得复制到脚本、文档、日志或提交内容。新增或变更表、字段、索引、约束、状态语义或数据迁移前，更新本文件并先确认迁移与回滚方案。
