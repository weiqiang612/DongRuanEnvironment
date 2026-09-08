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

`AqiFeedback` 请求体使用驼峰字段，例如 `telId`、`provinceId`、`cityId`、`estimatedGrade`、`afDate`、`afTime`。改变路径、方法、字段或响应结构前，更新本文件并确认前端调用同步修改。

## 目标契约原则

需求文档定义的认证、任务、检测、统计和 HR 接口尚未实现，以下是后续设计必须遵守的契约边界，而不是可调用的当前 API：

| 目标接口 | 调用方 | 责任与约束 |
|---|---|---|
| 用户认证 | NEPS、NEPG、NEPM | 公众注册、登录或员工身份校验；成功后签发访问令牌。 |
| 公众反馈 | NEPS | 创建反馈、查询本人反馈和处理进度；服务端验证反馈归属。 |
| 检测任务指派 | NEPM | 指派或重新指派网格员；服务端查询 HR 工作状态并记录指派日志。 |
| 实测 AQI 提交 | NEPG | 校验三项浓度，服务端计算最终 AQI 并完成任务；禁止由客户端传入最终值。 |
| 统计与预警查询 | NEPM、NEPV | 仅返回按角色授权的数据，统计口径为有效完成的检测结果。 |
| HR 人员查询 | 服务端 | 查询员工、区域和工作状态；不向浏览器端暴露。 |

目标接口统一使用 UTF-8 JSON；生产传输使用 HTTPS。认证确定为令牌方案时，请求应使用 `Authorization: Bearer <token>`，响应统一包含 `code`、`message`、`data`。具体路径、字段、错误码、幂等与分页规则必须在功能任务的设计阶段补齐，并经前后端共同确认后才可实施。
