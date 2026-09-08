# TASK-003: Tasks

**Spec**: `spec.md`
**Status**: Not started

## Key decisions

- 先更新接口、实体映射和 UI 当前页面记录，再调整实现；不以删改接口契约换取代码整洁。
- 反馈与地区接口保留既有路径和 HTTP 方法；DTO 仅改善校验和边界，不改变既有 JSON 字段。
- 前端保留 `AqiFeedBackList.vue` 作为当前唯一业务页，根路径重定向到该页，不新增业务 UI。

## Progress

- [ ] T1 — 更新 `api_contract.md`、`db_schema.md` 和 `ui_prototype.md` 的基线契约 · covers: AC-001, AC-002, AC-004, AC-UI-UX
- [ ] T2 — 重构后端 DTO、校验、服务和数据访问边界，保持正式接口兼容 · covers: AC-001, AC-002
- [ ] T3 — 对齐 `AqiFeedback` 实体类型与现有数据库字段，移除控制器直连数据访问 · covers: AC-001, AC-002
- [ ] T4 — 删除测试控制器、代码生成器、硬编码连接信息和仅供生成器使用的依赖，修复 UTF-8 编译编码 · covers: AC-003
- [ ] T5 — 重构前端 API 类型与反馈维护视图，移除默认脚手架并设置根路径重定向 · covers: AC-004, AC-UI-UX
- [ ] T6 — 补充或调整后端测试，覆盖合法请求与校验失败路径 · covers: AC-001, AC-002, AC-003
- [ ] T7 — 运行 `cd back && ./mvnw test` · covers: AC-001, AC-002, AC-003
- [ ] T8 — 运行 `cd front && npm run build` 和 `cd front && npm run lint` · covers: AC-004, AC-UI-UX
- [ ] T9 — 使用 Chrome MCP 完成 /aqiFeedback 的桌面、移动端、悬停、控制台和截图验收 · covers: AC-UI-UX
- [ ] T10 — 验证 AC，并更新 `spec.md` 的通过状态 · covers: AC-001, AC-002, AC-003, AC-004, AC-UI-UX
- [ ] T11 — 更新 `docs/4-tasks/CURRENT_PLAN.md`，标记任务完成。

## Dependencies

- T2、T3、T4 和 T5 依赖 T1。
- T6 依赖 T2、T3、T4。
- T7 依赖 T6；T8 和 T9 依赖 T5。
- T10 和 T11 依赖全部实现与验证任务。

## Blockers

<!-- Fill in if something is preventing progress -->
