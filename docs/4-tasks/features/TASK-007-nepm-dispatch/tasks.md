# TASK-007: Tasks

**Spec**: `spec.md`
**Status**: 功能开发完成（浏览器人工验收延后）

## Key decisions

- 候选网格员仅以本地 `grid_member.state = 0` 作为可工作条件，按同城市优先、同省其他城市兜底；不接入东软 HR。
- NEPM 权限、操作管理员和调度状态由服务端从员工 Session 与 `admins` 解析；浏览器只提交目标网格员编号。
- 首次指派、重派和继续处理统一在服务层事务内更新反馈并写入 `task_assign_log`；已完成反馈禁止调度。
- 超时沿用既有待指派 2 小时、已指派 24 小时规则，不改变主状态语义；本任务只展示并处置超时事项。
- 管理端不展示模拟检测结果或最终 AQI；后续模块入口明确标记为“后续开放”。

## Progress

- [x] T1 — 更新 `requirements_analysis.md`，固化候选网格员的同城优先、同省兜底、可工作性与调度规则 · covers: AC-002, AC-003, AC-004
- [x] T2 — 更新 `api_contract.md` 与 `architecture.md`，定义管理端查询、详情、候选人、指派/重派/继续处理、超时与运营概览的 Session、ResultVO、事务和错误边界 · covers: AC-001, AC-002, AC-003, AC-004, AC-005
- [x] T3 — 更新 `ui_prototype.md`，以用户提供的原型为准记录 NEPM 工作台、反馈管理、调度、超时预警、运营概览和后续开放入口的页面结构与交互 · covers: AC-001, AC-002, AC-003, AC-UI-UX
- [x] T4 — 新增管理端请求/响应 DTO、反馈筛选与运营概览查询、候选网格员与指派日志 Mapper 映射，复用既有物理表结构 · covers: AC-001, AC-002, AC-003, AC-005
- [x] T5 — 实现服务层 NEPM Session 授权与管理员解析、候选人同城优先/同省兜底、超时查询和运营概览 · covers: AC-001, AC-002, AC-003, AC-004, AC-005
- [x] T6 — 实现首次指派、重派与继续处理的原子状态校验、反馈更新和指派日志事务；拒绝已完成、不可工作或状态冲突操作 · covers: AC-002, AC-003, AC-004, AC-005
- [x] T7 — 实现管理端 Controller 与 ResultVO 边界，提供反馈、候选人、调度、超时和运营概览 API · covers: AC-001, AC-002, AC-003, AC-004, AC-005
- [x] T8 — 补充后端测试，覆盖管理端授权、同城优先、同省兜底、无候选人、不可指派、日期边界与状态冲突响应 · covers: AC-001, AC-002, AC-003, AC-004, AC-005
- [x] T9 — 新增前端管理端 API 客户端、类型和路由，将 `/nepm/portal` 升级为工作台并提供反馈管理、任务调度、超时预警、运营概览与后续开放入口 · covers: AC-001, AC-002, AC-003, AC-UI-UX
- [x] T10 — 按用户提供的 NEPM 原型实现工作台与各管理页面，完成筛选、详情、候选人、指派确认、重派、继续处理、空状态和错误提示交互 · covers: AC-001, AC-002, AC-003, AC-UI-UX
- [x] T11 — 运行 `cd back && ./mvnw test`（23 项通过） · covers: AC-001, AC-002, AC-003, AC-004, AC-005
- [x] T12 — 运行 `cd front && npm run build` 与 `cd front && npm run lint`（通过） · covers: AC-001, AC-UI-UX
- [ ] T13 — Chrome 桌面/移动端、悬停、控制台与命名截图验收延后；浏览器连接初始化异常，未将此项标记通过 · covers: AC-UI-UX
- [x] T14 — 已完成代码、接口与自动质量检查的验收记录；AC-UI-UX 的人工浏览器验收保留为 T13 补验 · covers: AC-001, AC-002, AC-003, AC-004, AC-005
- [x] T15 — 更新 `docs/4-tasks/CURRENT_PLAN.md` 与 `docs/4-tasks/DEVELOPMENT_ROADMAP.md`，记录实际验证结果与后续任务状态 · covers: doc-maintenance

## Dependencies

- T2、T3 依赖 T1；T4 至 T7 依赖 T2；T8 依赖 T4 至 T7；T9、T10 依赖 T2、T3、T7。
- T11 至 T15 依赖全部实现与验收任务；T10 在收到用户提供的 NEPM 原型后实施。

## Deferred validation

- 浏览器自动化连接初始化异常，未完成 T13 的桌面、移动端、悬停和控制台人工验收。该项不阻塞进入 TASK-008，后续可在任一前端联调会话补验。
