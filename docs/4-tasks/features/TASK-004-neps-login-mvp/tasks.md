# TASK-004: Tasks

**Spec**: `spec.md`
**Status**: Not started

## Key decisions

- 以 `supervisor` 为 NEPS 账号来源，手机号为唯一登录标识；本期不新增独立用户表或注册流程。
- 密码使用 JDK PBKDF2 哈希校验，认证成功后创建最小 HTTP Session；不引入安全框架、JWT 或外部认证服务。
- 统一入口仅开放 NEPS；登录成功跳转 `/aqiFeedback`，其他端与欢迎页留待后续任务。

## Progress

- [ ] T1 — 更新 `project_overview.md` 和 `requirements_analysis.md`，明确 NEPS 登录 MVP 与非目标 · covers: doc-maintenance, AC-001, AC-004
- [ ] T2 — 更新 `architecture.md`，记录 NEPS 认证服务、Session 与分层边界 · covers: doc-maintenance, AC-001, AC-003
- [ ] T3 — 更新 `api_contract.md`，定义 `POST /auth/neps/login` 的请求、成功及失败响应契约 · covers: AC-001, AC-002, AC-003
- [ ] T4 — 更新 `db_schema.md` 并新增可回滚的 supervisor 密码哈希迁移 · covers: AC-001, AC-003, AC-004
- [ ] T5 — 更新 `ui_prototype.md`，记录 PC 统一入口和 NEPS 登录页的路由、素材与交互范围 · covers: doc-maintenance, AC-004, AC-UI-UX
- [ ] T6 — 实现 supervisor 账号映射、密码哈希校验、认证服务与最小 HTTP Session · covers: AC-001, AC-002, AC-003
- [ ] T7 — 实现 NEPS 登录控制器、请求 DTO 和一致的错误响应 · covers: AC-001, AC-002, AC-003
- [ ] T8 — 实现统一入口、NEPS PC 登录页、认证 API 客户端和登录后跳转，复用现有 assets 素材 · covers: AC-001, AC-002, AC-004, AC-UI-UX
- [ ] T9 — 补充后端测试，覆盖正确密码、空参数、未知手机号和错误密码 · covers: AC-001, AC-002, AC-003
- [ ] T10 — 运行 `cd back && ./mvnw test` · covers: AC-001, AC-002, AC-003
- [ ] T11 — 运行 `cd front && npm run build` 和 `cd front && npm run lint` · covers: AC-001, AC-002, AC-004
- [ ] T12 — 使用 Chrome 完成 PC 端统一入口和 NEPS 登录页的 1440x900、悬停、控制台与命名截图验收 · covers: AC-UI-UX
- [ ] T13 — 验证 AC，并更新 `spec.md` 的通过状态 · covers: AC-001, AC-002, AC-003, AC-004, AC-UI-UX
- [ ] T14 — 更新 `docs/4-tasks/CURRENT_PLAN.md`，标记任务完成。

## Dependencies

- T2 至 T5 依赖 T1；T6、T7 依赖 T2 至 T4；T8 依赖 T3、T5、T7。
- T9 依赖 T6、T7；T10 依赖 T9；T11、T12 依赖 T8。
- T13、T14 依赖所有实现和验证任务。

## Blockers

<!-- Fill in if something is preventing progress -->
