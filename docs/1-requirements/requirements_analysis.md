# 需求分析

## 当前用户场景

管理员需要查看和维护空气质量公众反馈；录入或编辑反馈时，需要选择所在省、市并填写地址、反馈说明、预估 AQI、日期和时间。

## 当前业务流

前端 `AqiFeedBackList.vue` 调用 `/api/aqiFeedback/*`；Vite 去除 `/api` 前缀并转发至 Spring Boot。后端通过 MyBatis-Plus 操作 `aqi_feedback`，区域选项由 `grid_province` 与 `grid_city` 查询提供。

## 待澄清事项

- 删除接口目前使用 `GET`，未来契约调整前需确认兼容性。
- 写操作尚未发现鉴权与输入校验；补齐时需先定义权限和错误响应。
