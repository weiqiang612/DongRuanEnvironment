# 接口设计与契约

后端响应统一为 `ResultVO`，前端经 `/api` 代理调用。以下为已扫描基线。

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
