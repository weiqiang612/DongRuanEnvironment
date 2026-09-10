# 接口设计与契约

## 已实现基线

后端响应统一为 `ResultVO`，前端经 `/api` 代理调用。以下接口已扫描。

| 方法 | 后端路径 | 用途 |
|---|---|---|
| GET | `/aqiFeedback/list` | 查询反馈列表 |
| GET | `/aqiFeedback/find/{afId}` | 查询单条反馈 |
| GET | `/aqiFeedback/delete/{afId}` | 删除反馈 |
| POST | `/aqiFeedback/save` | 保存反馈 |
| POST | `/aqiFeedback/update` | 更新反馈 |
| GET | `/region/provinces` | 查询省选项 |
| GET | `/region/cities?provinceId={id}` | 查询市选项 |
| POST | `/auth/neps/login` | 公众监督员登录 |
| POST | `/auth/nepg/login` | 网格员登录 |
| POST | `/auth/nepm/login` | 系统管理员登录 |
| POST | `/auth/nepv/login` | 决策者登录 |

反馈保存和更新请求保持驼峰字段兼容：`afId`（仅更新必填）、`telId`、`provinceId`、`cityId`、`address`、`information`、`estimatedGrade`、`afDate`、`afTime`。`state`、指派字段、规范时间和超时字段由服务端维护，反馈表单不得传入或修改。

所有当前接口继续返回 `ResultVO` 外层：`code`、`message`、`data`。请求字段缺失、格式错误或 `estimatedGrade` 不在 0 至 6 时，返回 HTTP 400 与 `code: 400`，`data` 为字段错误信息；不存在的反馈返回 HTTP 404 与 `code: 404`，不暴露异常堆栈或数据库细节。`provinceId` 缺失时，`/region/cities` 同样按该规则拒绝。

### NEPS 登录 MVP

`POST /auth/neps/login` 请求体为 `{ "telId": "13800000000", "password": "用户输入密码" }`。两项均为必填字符串，前端只通过 HTTPS 或本地开发代理提交，绝不保存密码。

成功时返回 HTTP 200、`code: 200`，`data` 仅含 `telId`、`realName`；服务端创建 HTTP Session，密码与密码哈希不出现在响应中。缺失字段返回 HTTP 400、`code: 400` 和字段错误；手机号不存在或密码错误统一返回 HTTP 401、`code: 401`、`message: "手机号或密码错误"`，`data` 为 `null`。登录接口不提供注册、找回密码、令牌或其他角色认证。

### 员工端登录

`POST /auth/nepg/login`、`POST /auth/nepm/login`、`POST /auth/nepv/login` 的请求体统一为 `{ "accountCode": "业务编号", "password": "用户输入密码" }`。NEPG 的 `accountCode` 对应 `grid_member.gm_code`；NEPM、NEPV 对应 `admins.admin_code`。两项均为必填字符串，账号最长 20 个字符，前端绝不保存密码。

成功时返回 HTTP 200、`code: 200`，`data` 仅含 `accountCode`、`displayName`、`role`，其中角色分别为 `NEPG_GRID_MEMBER`、`NEPM_ADMIN`、`NEPV_DECISION_MAKER`。服务端仅向 HTTP Session 写入这三项非敏感身份信息。缺失字段返回 HTTP 400；未知账号、错误密码、`admins.role_code` 为空或角色与入口不匹配时统一返回 HTTP 401、`code: 401`、`message: "账号或密码错误"`，不得泄露账号或角色存在性。

## 目标契约原则

需求文档定义的认证、任务、检测、统计和 HR 接口尚未实现，以下是后续设计必须遵守的契约边界，而不是可调用的当前 API：

| 目标接口 | 调用方 | 责任与约束 |
|---|---|---|
| 用户认证 | NEPS、NEPG、NEPM、NEPV | 公众或员工身份校验；当前使用最小 HTTP Session，后续认证方式变更须独立设计。 |
| 公众反馈 | NEPS | 创建反馈、查询本人反馈和处理进度；服务端验证反馈归属。 |
| 检测任务指派 | NEPM | 指派或重新指派网格员；服务端查询 HR 工作状态并记录指派日志。 |
| 实测 AQI 提交 | NEPG | 校验三项浓度，服务端计算最终 AQI 并完成任务；禁止由客户端传入最终值。 |
| 统计与预警查询 | NEPM、NEPV | 仅返回按角色授权的数据，统计口径为有效完成的检测结果。 |
| HR 人员查询 | 服务端 | 查询员工、区域和工作状态；不向浏览器端暴露。 |

目标接口统一使用 UTF-8 JSON；生产传输使用 HTTPS。认证确定为令牌方案时，请求应使用 `Authorization: Bearer <token>`，响应统一包含 `code`、`message`、`data`。具体路径、字段、错误码、幂等与分页规则必须在功能任务的设计阶段补齐，并经前后端共同确认后才可实施。
