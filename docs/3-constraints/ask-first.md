# Ask First

## Dependencies and structure

- ⚠️ 新增或升级运行时依赖、插件、框架或脚手架前先确认。
- ⚠️ 新增服务、前端子应用、公共模块或大范围目录迁移前先确认。

## API and database

- ⚠️ 调整反馈状态流、改用 `DELETE` 接口、修改 `ResultVO` 响应格式等破坏性契约变更前先确认。
- ⚠️ 新增或修改数据库迁移、表、字段、索引、约束与数据清理操作前先确认。

## Environment

- ⚠️ 修改数据库地址、凭据策略、端口、部署文件或 CI 配置前先确认。

## From dev-standards repo

来源：`weiqiang612/dev-standards` 的 `universal/zh/ask-first.md`、`java/zh/java-core.md`、`java/zh/spring-conventions.md`、`vue/zh/vue-core.md`、`vue/zh/state-management.md`。

### 通用

- ⚠️ 修改 CI/CD、Dockerfile、生产环境配置、云环境设置、核心目录结构或基础设施命名规则前先确认。
- ⚠️ 新增外部包、Maven 或 Node 依赖、包管理器文件、架构模式、工具框架或横切关注点库前先确认。
- ⚠️ 升级 JDK、Node LTS、Vue 等语言或主框架大版本前先确认。

### Java 与 Spring Boot

- ⚠️ 新增线程池、调整线程池容量或 JVM 参数前先确认。
- ⚠️ 新增 MapStruct 全局默认配置、复杂多数据源映射或整体包分层调整前先确认。
- ⚠️ 新增全局 Filter、拦截器、复杂 AOP 切面或自定义 Spring Boot Starter 前先确认。

### Vue 3 与 Pinia

- ⚠️ 新增全局指令、全局插件、第三方 UI 组件库、绕过 Pinia 的全局事件机制或高度复杂的 composable 前先确认。
- ⚠️ 新增复杂 Pinia 插件、持久化驱动、跨 Store 循环依赖，或跨多个核心业务域的全局 Store 前先确认。
