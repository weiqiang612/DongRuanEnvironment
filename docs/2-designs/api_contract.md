# 接口设计与契约

## 已实现基线

后端响应统一为 `ResultVO`，前端经 `/api` 代理调用。以下接口已扫描。

| 方法 | 后端路径 | 用途 |
|---|---|---|
| GET | `/aqiFeedback/list` | 查询当前 NEPS 登录公众的反馈列表 |
| GET | `/aqiFeedback/find/{afId}` | 查询当前 NEPS 登录公众的一条反馈 |
| GET | `/aqiFeedback/delete/{afId}` | 删除当前 NEPS 登录公众的待指派反馈 |
| POST | `/aqiFeedback/save` | 以当前 NEPS 登录公众身份保存反馈 |
| POST | `/aqiFeedback/update` | 更新当前 NEPS 登录公众的待指派反馈 |
| GET | `/region/provinces` | 查询省选项 |
| GET | `/region/cities?provinceId={id}` | 查询市选项 |
| POST | `/auth/neps/login` | 公众监督员登录 |
| POST | `/auth/neps/register` | 公众监督员极简自主注册（手机号+密码） |
| POST | `/auth/neps/logout` | 公众监督员退出登录并销毁当前 Session |
| POST | `/auth/nepg/login` | 网格员登录 |
| POST | `/auth/nepm/login` | 系统管理员登录 |
| POST | `/auth/nepv/login` | 决策者登录 |
| POST | `/auth/logout` | 通用退出登录并销毁当前 Session |
| GET | `/auth/session` | 校验当前浏览器 Session 并返回所属端角色与展示名称 |

反馈保存和更新请求使用驼峰字段：`afId`（仅更新必填）、`provinceId`、`cityId`、`address`、`information`、`estimatedGrade`。`telId`、`afDate`、`afTime`、`state`、指派字段、规范时间和超时字段均由服务端维护，反馈表单不得传入或修改。

所有当前接口继续返回 `ResultVO` 外层：`code`、`message`、`data`。请求字段缺失、格式错误或 `estimatedGrade` 不在 0 至 6 时，返回 HTTP 400 与 `code: 400`，`data` 为字段错误信息；不存在的反馈返回 HTTP 404 与 `code: 404`，不暴露异常堆栈或数据库细节。`provinceId` 缺失时，`/region/cities` 同样按该规则拒绝。

### NEPS 反馈归属与生命周期

`/aqiFeedback` 的五个反馈接口均要求 NEPS HTTP Session 中存在 `nepsSupervisorTelId`。未登录时返回 HTTP 401、`code: 401`、`message: "请先登录"`；接口不接受 `telId`、`afDate`、`afTime`、状态或指派字段作为可写请求字段。保存时服务端使用 Session 手机号建立归属并写入当前日期时间；列表与详情只返回该手机号归属的记录。记录存在关联 `detection_result` 时，列表与详情额外返回只读 `finalAqiId`，供公众端展示网格员提交后的系统最终 AQI；尚未产生检测结果时该字段为 `null`。详情不存在或不归属当前公众时统一返回 HTTP 404，避免泄露其他公众的反馈内容。

更新与删除额外要求记录归属当前公众、`state = 0` 且 `timeoutFlag = false`。不符合条件时返回 HTTP 403、`code: 403`、`message: "该反馈当前不可修改"`，不得改变原记录；前端据此将非待指派或超时反馈作为只读展示。删除接口暂保持 `GET` 以兼容当前前端，后续改为 `DELETE` 必须独立评估。

### NEPS 登录 MVP

`POST /auth/neps/login` 请求体为 `{ "telId": "13800000000", "password": "用户输入密码" }`。两项均为必填字符串，前端只通过 HTTPS 或本地开发代理提交，绝不保存密码。

成功时返回 HTTP 200、`code: 200`，`data` 仅含 `telId`、`realName`；服务端创建 HTTP Session，密码与密码哈希不出现在响应中。缺失字段返回 HTTP 400、`code: 400` 和字段错误；手机号不存在或密码错误统一返回 HTTP 401、`code: 401`、`message: "手机号或密码错误"`，`data` 为 `null`。

`POST /auth/neps/register` 请求体为 `{ "telId": "13800000000", "password": "用户设置密码" }`。两项均为必填项，`telId` 必须符合大陆 11 位手机号正则（`^1[3-9]\d{9}$`），`password` 长度为 6-32 位。服务端自动完成排重校验与 PBKDF2 哈希加密入库，并自动注入脱敏初始属性（如真实姓名 `环保监督员_后4位`、默认生日与性别）。成功时返回 HTTP 200、`code: 200`、`data: true`；参数校验失败返回 HTTP 400；手机号已存在时返回 HTTP 409、`code: 409`、`message: "该手机号已注册"`。

`POST /auth/neps/logout` 与 `POST /auth/logout` 销毁当前浏览器的 HTTP Session，成功均返回 HTTP 200、`code: 200`、`data: true`。前端退出成功后返回登录入口；后续访问受保护业务接口均返回 HTTP 401。

### 员工端登录

`POST /auth/nepg/login`、`POST /auth/nepm/login`、`POST /auth/nepv/login` 的请求体统一为 `{ "accountCode": "业务编号", "password": "用户输入密码" }`。NEPG 的 `accountCode` 对应 `grid_member.gm_code`；NEPM、NEPV 对应 `admins.admin_code`。两项均为必填字符串，账号最长 20 个字符，前端绝不保存密码。

成功时返回 HTTP 200、`code: 200`，`data` 仅含 `accountCode`、`displayName`、`role`，其中角色分别为 `NEPG_GRID_MEMBER`、`NEPM_ADMIN`、`NEPV_DECISION_MAKER`。服务端仅向 HTTP Session 写入这三项非敏感身份信息。缺失字段返回 HTTP 400；未知账号、错误密码、`admins.role_code` 为空或角色与入口不匹配时统一返回 HTTP 401、`code: 401`、`message: "账号或密码错误"`，不得泄露账号或角色存在性。

`GET /auth/session` 不创建新 Session，仅校验当前浏览器已有 Session。会话有效时返回 HTTP 200、`data.portal`：公众监督员为 `NEPS_SUPERVISOR`，员工端分别为 `NEPG_GRID_MEMBER`、`NEPM_ADMIN`、`NEPV_DECISION_MAKER`；会话不存在、已失效或身份字段不完整时返回 HTTP 401、`code: 401`、`message: "请先登录"`。四端前端路由进入受保护页面前必须调用该接口，并在业务请求返回 401 时跳转到本端登录页。

### NEPM 管理者工作台与任务调度（TASK-007）

以下接口均要求员工 Session 同时具备 `employeeRole = NEPM_ADMIN` 与非空 `employeeAccountCode`。Session 缺失时返回 HTTP 401、`code: 401`、`message: "请先登录"`；角色不匹配、管理员账号不能解析或操作越权时返回 HTTP 403、`code: 403`、`message: "无管理端权限"`。所有响应保持 `ResultVO` 外层，服务端不得返回密码、Session 内容或未授权员工信息。

| 方法 | 后端路径 | 请求 | 成功 `data` | 失败边界 |
|---|---|---|---|---|
| GET | `/nepm/dashboard` | 无 | 待指派、已指派、已完成、超时数量与近期待办反馈 | 无权限按管理端 Session 规则拒绝。 |
| GET | `/nepm/feedbacks` | 查询参数 `provinceId`、`cityId`、`states`、`timeoutOnly`、`estimatedGrade`、`submittedFrom`、`submittedTo`、`keyword`、`page`、`pageSize` 均可选；`states` 可重复传入 | `{ items, total, page, pageSize }`；每项含反馈、地区名称、当前网格员名称和展示状态 | 日期、枚举、分页参数不合法返回 HTTP 400。 |
| GET | `/nepm/feedbacks/{afId}` | 路径 `afId` | 反馈详情、当前网格员、指派日志和处理流程展示数据 | 不存在返回 HTTP 404。 |
| GET | `/nepm/feedbacks/{afId}/candidates` | 路径 `afId` | 同城优先、同省其他城市兜底的可工作网格员列表；每项含人员、地区和来源层级 | 不存在返回 HTTP 404；同省无候选人时返回空列表而非虚构候选人。 |
| POST | `/nepm/feedbacks/{afId}/dispatch` | `{ "gridMemberId": "网格员手机号" }` | 更新后的反馈调度信息和新写入的指派日志 | 已完成、候选人不可工作或反馈状态已变化返回 HTTP 409、`code: 409`；请求不得携带操作人、状态、超时标识或日志字段。 |
| GET | `/nepm/timeout-alerts` | `provinceId`、`cityId`、`states`、`submittedFrom`、`submittedTo`、`keyword`、`page`、`pageSize` 均可选 | 超时反馈分页数据与当前网格员 | 仅返回既有超时标识为真的反馈；预警处置状态留待预警模块实现。 |
| GET | `/nepm/analytics/overview` | 可选 `provinceId`、`cityId`、`submittedFrom`、`submittedTo` | 反馈总数及待指派、已指派、已完成、超时数量 | 只基于反馈与调度数据，不返回最终 AQI、检测结果或 AQI 预警统计。 |
| GET | `/nepm/analytics/recent-trend` | 无 | `{ dates, newFeedbacks, completedFeedbacks, pendingFeedbacks }`；最近七个自然日的新增反馈、当日办结及日终待处理数量 | 无权限按管理端 Session 规则拒绝；无业务数据时返回七日零值，不得补造样本或截断统计值。 |

`POST /nepm/feedbacks/{afId}/dispatch` 由服务端依据当前反馈和目标人员决定日志动作：`state = 0` 时记录 `ASSIGN`；已指派或已超时且目标人员不同于当前 `gm_id` 时记录 `REASSIGN`；目标人员等于当前 `gm_id` 时记录 `CONTINUE` 且不改变指派事实。服务层在一个事务中完成状态校验、候选可工作性校验、反馈更新和日志写入；前端不传入或覆盖 `adminId`、`operatorId`、`state`、`timeoutFlag`、时间字段或 `actionType`。

`GET /nepm/analytics/recent-trend` 的新增反馈按 `submitted_at` 的自然日归属，当日办结按 `completed_at` 的自然日归属；两者是独立流量指标，不计算或返回“办结率”。`pendingFeedbacks`（页面名称“日终待处理”）表示每个自然日结束时，已提交且当时未完成的反馈数量：提交日在该日或此前，且完成时间为空或晚于该日。对仅有历史日期字段的已完成记录，完成日按其提交日兼容处理。该接口只返回真实查询结果，零值保持为零。

### NEPG 网格员任务与实测（TASK-008）

以下接口要求员工 Session 同时具备 `employeeRole = NEPG_GRID_MEMBER` 与非空 `employeeAccountCode`；未登录返回 HTTP 401，角色不符或跨网格员访问返回 HTTP 403。客户端不得传入网格员编号、最终等级、污染物等级、状态、检测时间、完成时间或预警事实。

| 方法 | 后端路径 | 请求 | 成功 `data` | 失败边界 |
|---|---|---|---|---|
| GET | `/nepg/tasks` | 无 | 本人 `state = 1/2` 任务列表，含地区、地址、描述、公众预估等级、状态、超时和派发/完成时间 | 仅返回当前 Session 解析的网格员任务。 |
| GET | `/nepg/tasks/{afId}` | 路径 `afId` | 任务详情；已完成时额外含最终等级、三项实测值、检测时间及预警是否生成 | 不存在为 404；非本人为 403。 |
| POST | `/nepg/tasks/{afId}/measurements` | `{ "so2Value": number, "coValue": number, "spmValue": number }` | 已完成任务详情 | 负数、精度不合法或无法唯一匹配 AQI 区间为 400；已完成、未指派或竞争失败为 409。 |

提交接口只接受上述三个 JSON 字段；服务端在事务中完成字典校验、最终 AQI 计算、`detection_result` 插入、`aqi_feedback.state = 2` 与 `completed_at` 更新，以及等级 4 至 6 的 `alert_record` 插入。超时标识只读保留，不因提交被覆盖。

### NEPM 检测处置与运营统计（TASK-009）

以下接口均要求员工 Session 同时具备 `employeeRole = NEPM_ADMIN` 与非空 `employeeAccountCode`。
- 未登录（Session 中无 `employeeRole` 或为空）：返回 HTTP 401、`code: 401`、`message: "请先登录"`。
- 角色不匹配或账号为空：返回 HTTP 403、`code: 403`、`message: "无管理端权限"`。
- 所有响应外层统一封装为 `ResultVO<T>`：`code`、`message`、`data`。

| 方法 | 后端路径 | 请求参数 / 请求体 | 成功 `data` | 失败边界 |
|---|---|---|---|---|
| GET | `/nepm/detection-results` | `provinceId`、`cityId`、`aqiId`（1~6）、`submittedFrom`、`submittedTo`、`keyword`、`page`（默认1）、`pageSize`（默认10，最大100）均可选 | `{ items, total, page, pageSize }`；每项包含检测结果ID、关联反馈、区域地址、网格员姓名、三项实测数值与分项等级、系统最终 AQI 等级及检测时间 | 参数不合法或日期格式错误返回 HTTP 400。 |
| GET | `/nepm/detection-results/{id}` | 路径参数 `id`（检测结果ID） | 单条检测结果详情对象，包含完整三项实测值、分项等级、最终 AQI、反馈描述、预估等级、网格员及流转时间 | 记录不存在返回 HTTP 404。 |
| GET | `/nepm/aqi-alerts` | `status`（`PENDING` / `HANDLED`）、`provinceId`、`cityId`、`page`（默认1）、`pageSize`（默认10，最大100）均可选 | `{ items, total, page, pageSize }`；每项包含预警ID、反馈ID、检测ID、预警等级（4~6）、处置状态、生成时间、处置时间、地区地址及最终 AQI | 状态值非法返回 HTTP 400。 |
| POST | `/nepm/aqi-alerts/{alertId}/handle` | 路径参数 `alertId`；请求体无额外写字段（可为空或 `{}`） | 处置成功的预警对象，包含 `id`、`alertStatus: "HANDLED"`、`handledAt` 等 | 记录不存在返回 HTTP 404；已处置或并发冲突（非 `PENDING`）返回 HTTP 409；禁止客户端传入写字段。 |
| GET | `/nepm/analytics/stats` | `provinceId`、`cityId`、`submittedFrom`、`submittedTo` 均可选 | 运营统计聚合对象，包含累计检测总量、1~6 级 AQI 等级分布、月度趋势、高等级预警汇总（总数、待处置、已处置） | 起止日期倒置或格式错误返回 HTTP 400。 |

#### 接口字段规格细化

1. **`GET /nepm/detection-results` & `GET /nepm/detection-results/{id}` 字段规格**
   - `id`: 检测结果主键（Integer）
   - `feedbackId`: 关联反馈编号（Integer）
   - `provinceId`: 省编号（Integer）
   - `provinceName`: 省名称（String）
   - `cityId`: 市编号（Integer）
   - `cityName`: 市名称（String）
   - `address`: 详细地址（String）
   - `information`: 反馈描述（String）
   - `estimatedGrade`: 公众预估等级（Integer, 0~6）
   - `gmId`: 网格员编号（String）
   - `gmName`: 网格员姓名（String）
   - `so2Value`: SO₂ 实测值（BigDecimal/Double）
   - `so2Level`: SO₂ 对应分项等级（Integer, 1~6）
   - `coValue`: CO 实测值（BigDecimal/Double）
   - `coLevel`: CO 对应分项等级（Integer, 1~6）
   - `spmValue`: PM2.5 实测值（BigDecimal/Double）
   - `spmLevel`: PM2.5 对应分项等级（Integer, 1~6）
   - `aqiId`: 系统最终 AQI 等级（Integer, 1~6）
   - `detectedAt`: 检测完成时间（LocalDateTime, 格式 `yyyy-MM-dd HH:mm:ss`）
   - `submittedAt`: 反馈提交时间（LocalDateTime, 格式 `yyyy-MM-dd HH:mm:ss`）

2. **`GET /nepm/aqi-alerts` 字段规格**
   - `id`: 预警记录主键（Long）
   - `feedbackId`: 关联反馈编号（Integer）
   - `resultId`: 关联检测结果编号（Integer）
   - `alertLevel`: 预警等级（Integer, 4~6）
   - `alertStatus`: 处置状态（String, `PENDING` 或 `HANDLED`）
   - `createdAt`: 预警生成时间（LocalDateTime）
   - `handledAt`: 处置时间（LocalDateTime，待处置时为 null）
   - `provinceId`: 省编号（Integer）
   - `provinceName`: 省名称（String）
   - `cityId`: 市编号（Integer）
   - `cityName`: 市名称（String）
   - `address`: 详细地址（String）
   - `aqiId`: 系统最终 AQI 等级（Integer）
   - `gmName`: 网格员姓名（String）

3. **`POST /nepm/aqi-alerts/{alertId}/handle` 处置原子规则**
   - 请求体严禁传入 `handledAt`、`alertStatus`、`operatorId` 等字段，服务端统一从当前 Session 校验管理员权限，并使用数据库当前时间 `NOW()` 记录 `handled_at`。
   - 更新执行原子条件更新：`UPDATE alert_record SET alert_status = 'HANDLED', handled_at = ? WHERE id = ? AND alert_status = 'PENDING'`。
   - 若返回影响行数为 0，且记录存在，则证明已被其他管理员处置或状态已不是 `PENDING`，服务端抛出并发冲突异常并向前端返回 HTTP 409、`code: 409`、`message: "预警已被处置或状态不符"`。

4. **`GET /nepm/analytics/stats` 综合运营与分析统计数据结构**
   - 支持参数：`provinceId`、`cityId`、`submittedFrom`、`submittedTo`
   - 响应数据结构：
   ```json
   {
     "code": 200,
     "message": "查询成功",
     "data": {
       "totalDetections": 127,
       "totalFeedbacks": 328,
       "completionRate": 92.3,
       "completionRateChange": 2.8,
       "timeoutRate": 5.1,
       "timeoutRateChange": -1.5,
       "highPollutionRate": 18.6,
       "highPollutionRateChange": 3.2,
       "periodInsight": "本周期共收到 328 条反馈，完成率 92.3%...",
       "regionRisks": [
         { "regionName": "朝阳区", "feedbackCount": 280, "highPollutionRate": 24.3 },
         { "regionName": "丰台区", "feedbackCount": 186, "highPollutionRate": 36.7 },
         { "regionName": "西湖区", "feedbackCount": 185, "highPollutionRate": 12.5 },
         { "regionName": "石家庄市", "feedbackCount": 135, "highPollutionRate": 18.0 },
         { "regionName": "海淀区", "feedbackCount": 120, "highPollutionRate": 18.1 },
         { "regionName": "唐山市", "feedbackCount": 95, "highPollutionRate": 25.0 }
       ],
       "efficiencyTrends": [
         { "month": "2026-04", "feedbackCount": 140, "completionRate": 78.5 },
         { "month": "2026-05", "feedbackCount": 195, "completionRate": 82.1 },
         { "month": "2026-06", "feedbackCount": 175, "completionRate": 85.6 },
         { "month": "2026-07", "feedbackCount": 220, "completionRate": 88.9 },
         { "month": "2026-08", "feedbackCount": 245, "completionRate": 91.2 },
         { "month": "2026-09", "feedbackCount": 280, "completionRate": 92.3 }
       ],
       "pollutionTrends": [
         { "month": "2026-04", "highPollutionRate": 12.3 },
         { "month": "2026-05", "highPollutionRate": 14.6 },
         { "month": "2026-06", "highPollutionRate": 16.8 },
         { "month": "2026-07", "highPollutionRate": 20.1 },
         { "month": "2026-08", "highPollutionRate": 22.4 },
         { "month": "2026-09", "highPollutionRate": 18.6 }
       ],
       "highAlertCount": 26,
       "pendingAlertCount": 0,
       "handledAlertCount": 26
     }
   }
   ```

5. **任务调度管理合并与超时调度**
   - NEPM 管理端“任务分派”与“任务处理”界面合并为统一的【任务管理】界面，上部通过 Tabs 状态选项卡（全部、待指派、已指派、已完成、已超时）切换，括号带黑色计数统计；
   - 筛选区统一样式，地区筛选统一采用省市两级联动选择；
   - 超时任务调度与重派继续复用 `GET /nepm/timeout-alerts` 与 `POST /nepm/feedbacks/{afId}/dispatch` 接口。
   - 超时任务重新指派后，调度引擎自动完成网格员切换与 `task_assign_log` 的 `REASSIGN` 动作记录，保持调度核心逻辑复用与单一职责。

## 目标契约原则

需求文档定义的 NEPV 决策大屏与 HR 外部接口尚未实现，以下是后续设计必须遵守的契约边界，而不是可调用的当前 API：

| 目标接口 | 调用方 | 责任与约束 |
|---|---|---|
| 用户认证 | NEPS、NEPG、NEPM、NEPV | 公众或员工身份校验；当前使用最小 HTTP Session，后续认证方式变更须独立设计。 |
| 公众反馈 | NEPS | 创建反馈、查询本人反馈和处理进度；服务端验证反馈归属。 |
| 检测任务调度 | NEPM | 指派或重新指派网格员；复用本地网格员状态与指派日志，HR 实时状态查询留待独立任务。 |
| 实测 AQI 提交 | NEPG | 校验三项浓度，服务端计算最终 AQI 并完成任务；禁止由客户端传入最终值。 |
| 检测结果查阅与预警处置 | NEPM | 查询实测结果与分项等级；原子处置 4~6 级预警（HANDLED）；综合运营统计聚合。 |
| 决策分析与大屏展示 | NEPV | 面向决策者的宏观统计图表、网格覆盖率与高等级预警综合监控看板（TASK-010）。 |
| HR 人员查询 | 服务端 | 查询员工、区域和工作状态；不向浏览器端暴露。 |

目标接口统一使用 UTF-8 JSON；生产传输使用 HTTPS。认证确定为令牌方案时，请求应使用 `Authorization: Bearer <token>`，响应统一包含 `code`、`message`、`data`。具体路径、字段、错误码、幂等与分页规则必须在功能任务的设计阶段补齐，并经前后端共同确认后才可实施。
