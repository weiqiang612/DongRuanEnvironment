# TASK-002: Tasks

**Spec**: `spec.md`
**Status**: Complete

## Key decisions

- `back/` 是唯一的后端 Maven 根目录，不再保留后端二级模块目录。
- Java 根包使用 `com.dongruan.environment`；本任务不重命名应用启动类。
- 本任务只同步迁移附带引用，不改变接口、数据库或前端实现。

## Progress

- [x] T1 — 更新架构、数据库设计和后端测试命令的路径引用 · covers: doc-maintenance, AC-001, AC-002
- [x] T2 — 更新根/后端 AGENTS.md 与跨平台启动脚本 · covers: doc-maintenance, AC-001, AC-002
- [x] T3 — 清理代码生成器等有效源码中的旧 Java 包引用 · covers: AC-002, AC-003
- [x] T4 — 搜索旧目录和包名，并检查不复制凭据或修改迁移内容 · covers: AC-003, AC-004
- [x] T5 — 在 `back` 运行 `./mvnw test` · covers: AC-001
- [x] T6 — 验证 AC，并更新 `spec.md` 的通过状态 · covers: AC-001, AC-002, AC-003, AC-004
- [x] T7 — 更新 `docs/4-tasks/CURRENT_PLAN.md`，标记任务完成。

## Dependencies

- T2 和 T3 依赖 T1。
- T4 和 T5 依赖 T2、T3。
- T6 和 T7 依赖 T4、T5。

## Blockers

<!-- Fill in if something is preventing progress -->
