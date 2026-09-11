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
| POST | `/auth/neps/logout` | 公众监督员退出登录并销毁当前 Session |
| POST | `/auth/nepg/login` | 网格员登录 |
| POST | `/auth/nepm/login` | 系统管理员登录 |
| POST | `/auth/nepv/login` | 决策者登录 |

反馈保存和更新请求使用驼峰字段：`afId`（仅更新必填）、`provinceId`、`cityId`、`address`、`information`、`estimatedGrade`。`telId`、`afDate`、`afTime`、`state`、指派字段、规范时间和超时字段均由服务端维护，反馈表单不得传入或修改。

所有当前接口继续返回 `ResultVO` 外层：`code`、`message`、`data`。请求字段缺失、格式错误或 `estimatedGrade` 不在 0 至 6 时，返回 HTTP 400 与 `code: 400`，`data` 为字段错误信息；不存在的反馈返回 HTTP 404 与 `code: 404`，不暴露异常堆栈或数据库细节。`provinceId` 缺失时，`/region/cities` 同样按该规则拒绝。

### NEPS 反馈归属与生命周期

`/aqiFeedback` 的五个反馈接口均要求 NEPS HTTP Session 中存在 `nepsSupervisorTelId`。未登录时返回 HTTP 401、`code: 401`、`message: "请先登录"`；接口不接受 `telId`、`afDate`、`afTime`、状态或指派字段作为可写请求字段。保存时服务端使用 Session 手机号建立归属并写入当前日期时间；列表与详情只返回该手机号归属的记录。详情不存在或不归属当前公众时统一返回 HTTP 404，避免泄露其他公众的反馈内容。

更新与删除额外要求记录归属当前公众、`state = 0` 且 `timeoutFlag = false`。不符合条件时返回 HTTP 403、`code: 403`、`message: "该反馈当前不可修改"`，不得改变原记录；前端据此将非待指派或超时反馈作为只读展示。删除接口暂保持 `GET` 以兼容当前前端，后续改为 `DELETE` 必须独立评估。

### NEPS 登录 MVP

`POST /auth/neps/login` 请求体为 `{ "telId": "13800000000", "password": "用户输入密码" }`。两项均为必填字符串，前端只通过 HTTPS 或本地开发代理提交，绝不保存密码。

成功时返回 HTTP 200、`code: 200`，`data` 仅含 `telId`、`realName`；服务端创建 HTTP Session，密码与密码哈希不出现在响应中。缺失字段返回 HTTP 400、`code: 400` 和字段错误；手机号不存在或密码错误统一返回 HTTP 401、`code: 401`、`message: "手机号或密码错误"`，`data` 为 `null`。登录接口不提供注册、找回密码、令牌或其他角色认证。

`POST /auth/neps/logout` 销毁当前浏览器的 NEPS HTTP Session，成功返回 HTTP 200、`code: 200`、`data: true`。前端退出成功后返回统一登录入口；后续访问反馈接口会按未登录处理。

### 员工端登录

`POST /auth/nepg/login`、`POST /auth/nepm/login`、`POST /auth/nepv/login` 的请求体统一为 `{ "accountCode": "业务编号", "password": "用户输入密码" }`。NEPG 的 `accountCode` 对应 `grid_member.gm_code`；NEPM、NEPV 对应 `admins.admin_code`。两项均为必填字符串，账号最长 20 个字符，前端绝不保存密码。

成功时返回 HTTP 200、`code: 200`，`data` 仅含 `accountCode`、`displayName`、`role`，其中角色分别为 `NEPG_GRID_MEMBER`、`NEPM_ADMIN`、`NEPV_DECISION_MAKER`。服务端仅向 HTTP Session 写入这三项非敏感身份信息。缺失字段返回 HTTP 400；未知账号、错误密码、`admins.role_code` 为空或角色与入口不匹配时统一返回 HTTP 401、`code: 401`、`message: "账号或密码错误"`，不得泄露账号或角色存在性。

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

`POST /nepm/feedbacks/{afId}/dispatch` 由服务端依据当前反馈和目标人员决定日志动作：`state = 0` 时记录 `ASSIGN`；已指派或已超时且目标人员不同于当前 `gm_id` 时记录 `REASSIGN`；目标人员等于当前 `gm_id` 时记录 `CONTINUE` 且不改变指派事实。服务层在一个事务中完成状态校验、候选可工作性校验、反馈更新和日志写入；前端不传入或覆盖 `adminId`、`operatorId`、`state`、`timeoutFlag`、时间字段或 `actionType`。

## 目标契约原则

需求文档定义的认证、任务、检测、统计和 HR 接口尚未实现，以下是后续设计必须遵守的契约边界，而不是可调用的当前 API：

| 目标接口 | 调用方 | 责任与约束 |
|---|---|---|
| 用户认证 | NEPS、NEPG、NEPM、NEPV | 公众或员工身份校验；当前使用最小 HTTP Session，后续认证方式变更须独立设计。 |
| 公众反馈 | NEPS | 创建反馈、查询本人反馈和处理进度；服务端验证反馈归属。 |
| 检测任务指派 | NEPM | 指派或重新指派网格员；TASK-007 暂以本地网格员工作状态筛选候选人并记录指派日志，HR 实时状态查询留待独立任务。 |
| 实测 AQI 提交 | NEPG | 校验三项浓度，服务端计算最终 AQI 并完成任务；禁止由客户端传入最终值。 |
| 统计与预警查询 | NEPM、NEPV | 仅返回按角色授权的数据，统计口径为有效完成的检测结果。 |
| HR 人员查询 | 服务端 | 查询员工、区域和工作状态；不向浏览器端暴露。 |

目标接口统一使用 UTF-8 JSON；生产传输使用 HTTPS。认证确定为令牌方案时，请求应使用 `Authorization: Bearer <token>`，响应统一包含 `code`、`message`、`data`。具体路径、字段、错误码、幂等与分页规则必须在功能任务的设计阶段补齐，并经前后端共同确认后才可实施。
