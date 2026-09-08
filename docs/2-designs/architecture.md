# 系统架构设计

## 模块

- `back/demo`：Spring Boot REST API，端口 `8080`。
- `front`：Vue 3 单页应用；开发期将 `/api` 代理到后端。
- MySQL：由 Spring `JdbcTemplate` 和 MyBatis-Plus 访问。

## 分层

控制器处理路由与请求/响应；服务层承载业务逻辑；Mapper 与 `JdbcTemplate` 执行数据访问；`ResultVO` 统一封装接口响应。前端 API 客户端集中在 `front/src/api/`，视图位于 `front/src/views/`。

## 集成边界

前端请求路径必须带 `/api`，后端控制器不带该前缀。后端监听 `8080`；Swagger UI 为 `/swagger-ui.html`，OpenAPI 文档为 `/api-docs`。
