# TASK-005: Tasks

**Spec**: `spec.md`
**Status**: In Progress

## Key decisions

- NEPG 使用 `grid_member.gm_code`，NEPM 与 NEPV 使用 `admins.admin_code`；NEPV 通过 `NEPV_DECISION_MAKER` 角色识别，不新增决策者账号表。
- 统一复用 JDK PBKDF2、`ResultVO` 与最小 HTTP Session；认证失败不泄露账号、密码或角色细节。
- 三端登录成功只进入对应最小落地页，业务功能与全局权限控制留待独立任务。
- NEPS 与三端员工登录复用同一视图组件，但继续调用既有接口、使用手机号字段并跳转既有反馈页。
- 四端 HTTP 登录端点集中在 `AuthController`，但 NEPS 与员工端认证服务、DTO、校验和 Session 字段保持隔离。
- 四端共用登录页在登录卡片左上角提供轻量“← 返回”操作，统一跳转 `/`。

## Progress

- [x] T1 — 更新 `project_overview.md` 与 `requirements_analysis.md`，明确三端员工身份来源、管理员角色和非目标 · covers: doc-maintenance, AC-001, AC-003
- [x] T2 — 新增 ADR，记录 admins 复用为 NEPM/NEPV 身份来源的决策、替代方案和后果 · covers: doc-maintenance, AC-003
- [x] T3 — 更新 `architecture.md`，记录三端认证服务、角色校验和 Session 边界 · covers: doc-maintenance, AC-001, AC-003
- [x] T4 — 更新 `api_contract.md`，定义三个登录接口的请求、成功、字段错误和安全认证失败契约 · covers: AC-001, AC-002, AC-004
- [x] T5 — 更新 `db_schema.md`，并编写 grid_member 密码、admins 密码与角色的可回滚迁移；记录迁移前结构、行数、备份与凭据格式核验步骤 · covers: AC-003, AC-004
- [x] T6 — 更新 `ui_prototype.md`，记录三张 PC 登录页、最小落地页、路由与交互范围 · covers: doc-maintenance, AC-001, AC-UI-UX
- [x] T7 — 实现网格员和管理员的实体映射、认证 DTO/响应、Mapper 与共享认证服务；按角色隔离 NEPM、NEPV 认证 · covers: AC-001, AC-002, AC-003
- [x] T8 — 实现四端登录端点的统一 `AuthController` 入口及最小 Session 写入，保持各端认证服务、ResultVO 与失败响应一致 · covers: AC-001, AC-002, AC-004
- [x] T9 — 实现四端统一前端登录视图、员工端认证 API、统一入口跳转与三张最小落地页；保持 NEPS 既有接口和落地页不变 · covers: AC-001, AC-002, AC-UI-UX
- [ ] T10 — 补充后端测试，覆盖三端正确认证、空字段、未知账号、错误密码、角色为空和角色不匹配 · covers: AC-001, AC-002, AC-003, AC-004
- [ ] T11 — 使用 Chrome 完成 PC 端入口、三张登录页和三张最小落地页的桌面尺寸、悬停、控制台和命名截图验收 · covers: AC-UI-UX
- [ ] T12 — 运行 `cd back && ./mvnw test` · covers: AC-001, AC-002, AC-003, AC-004
- [ ] T13 — 运行 `cd front && npm run build` 与 `cd front && npm run lint` · covers: AC-001, AC-UI-UX
- [ ] T14 — 验证 AC，并更新 `spec.md` 的通过状态 · covers: AC-001, AC-002, AC-003, AC-004, AC-UI-UX
- [ ] T15 — 更新 `docs/4-tasks/CURRENT_PLAN.md`，标记任务完成 · covers: doc-maintenance

## Dependencies

- T2 至 T6 依赖 T1；T7、T8 依赖 T3 至 T5；T9 依赖 T4、T6、T8。
- T10 依赖 T7、T8；T11 依赖 T9；T12 至 T15 依赖全部实现和验收任务。

## Blockers

- 执行真实数据库迁移和浏览器成功登录验收前，需要用户配置真实账号与密码；不得以默认账号或测试密码代替。
