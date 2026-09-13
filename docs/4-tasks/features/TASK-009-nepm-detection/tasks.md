# TASK-009: Tasks

**Spec**: `spec.md`
**Status**: Completed

## Key decisions

- 检测结果是 TASK-008 生成的只读系统事实；管理端只负责查询和核对，不新增人工确认状态、结果修改或驳回流程。
- AQI 预警仅复用 `alert_record` 的 `PENDING`、`HANDLED` 与 `handled_at`；超时任务继续复用 TASK-007 的调度操作和 `task_assign_log`，两类数据不得混为一套预警记录。
- 管理统计直接聚合有效 `detection_result` 及其关联反馈和预警，不新增统计汇总表；NEPV 决策大屏和网格覆盖率留待 TASK-010。
- 不新增数据库结构、运行时依赖、身份方案或反馈状态，新增接口继续使用 NEPM HTTP Session 与 `ResultVO`。

## Progress

- [x] T1 — 更新 `docs/1-requirements/project_overview.md` 与 `docs/1-requirements/requirements_analysis.md`，同步 TASK-007、TASK-008 和公众注册的已实现状态，并固化 TASK-009/TASK-010 边界 · covers: doc-maintenance, AC-001, AC-004
- [x] T2 — 更新 `docs/2-designs/architecture.md`，定义 NEPM 检测结果只读查询、AQI 预警状态处理、超时调度复用及统计聚合链路 · covers: doc-maintenance, AC-001, AC-002, AC-003, AC-004, AC-005
- [x] T3 — 更新 `docs/2-designs/api_contract.md`，明确检测结果、AQI 预警与管理统计接口的筛选、分页、响应、Session、错误码和并发边界，并保持现有超时与调度契约 · covers: AC-001, AC-002, AC-003, AC-004, AC-005
- [x] T4 — 更新 `docs/2-designs/ui_prototype.md`，定义检测结果、AQI 预警、超时任务与统计分析的真实数据布局、状态和交互，删除对应“后续开放”描述 · covers: doc-maintenance, AC-001, AC-002, AC-003, AC-004, AC-UI-UX
- [x] T5 — 新增 NEPM 检测结果、AQI 预警和管理统计所需的查询对象、响应 DTO 与前端共享字段定义，复用现有实体及物理表 · covers: AC-001, AC-002, AC-004, AC-005
- [x] T6 — 扩展 Mapper 查询，完成检测结果分页、详情及关联反馈、地区、网格员和 AQI 字典信息读取 · covers: AC-001, AC-005
- [x] T7 — 实现 NEPM 检测结果只读服务与 Controller，提供组合筛选、分页和详情，并拒绝未授权访问及非法参数 · covers: AC-001, AC-005
- [x] T8 — 实现 AQI 预警分页、详情和 PENDING 到 HANDLED 的原子条件更新；重复或竞争处理返回 409，不写入额外处置事实 · covers: AC-002, AC-005
- [x] T9 — 校正超时视图的展示映射和操作边界，继续复用现有重派、继续处理及指派日志能力，已完成超时任务保持只读 · covers: AC-003, AC-005
- [x] T10 — 实现管理统计聚合，按省、市和时间范围返回检测数量、AQI 等级分布、月度趋势及高等级预警数量，并排除无检测结果的反馈 · covers: AC-004, AC-005
- [x] T11 — 补充后端测试，覆盖检测结果筛选与详情、只读事实、预警处理幂等与并发、超时/AQI 预警隔离、统计口径、NEPM 授权及参数错误 · covers: AC-001, AC-002, AC-003, AC-004, AC-005
- [x] T12 — 扩展 `front/src/api/nepm.ts` 的检测结果、AQI 预警和统计请求函数及 TypeScript 类型，统一处理分页和 `ResultVO` 响应 · covers: AC-001, AC-002, AC-004, AC-005
- [x] T13 — 将 NEPM 检测结果占位视图升级为真实列表、筛选、分页和只读详情，并严格区分公众预估 AQI 与系统最终 AQI · covers: AC-001, AC-UI-UX
- [x] T14 — 将 AQI 预警视图接入真实预警查询与处理，完善超时视图的既有调度入口和只读状态，清晰区分两类异常 · covers: AC-002, AC-003, AC-UI-UX
- [x] T15 — 扩展统计分析视图，展示真实检测数量、AQI 等级分布、月度趋势和高等级预警数量，并提供地区与时间筛选及空状态 · covers: AC-004, AC-UI-UX
- [x] T16 — 使用 Chrome MCP 验收 `/nepm/portal`，覆盖 1440x900、375x812、导航/筛选/列表/分页/处理按钮悬停、控制台零 JavaScript 错误，并存档 `TASK-009-nepm-detection-desktop.png`、`TASK-009-nepm-detection-mobile.png` · covers: AC-UI-UX
- [x] T17 — 运行 `cd back && ./mvnw test`，全部测试必须通过
- [x] T18 — 运行 `cd front && npm run build` 与 `cd front && npm run lint`，构建通过且无 Lint 违规
- [x] T19 — 逐项验证 `spec.md` 的验收标准，并将通过项的 `passes` 更新为 `true`
- [x] T20 — 更新 `docs/4-tasks/DEVELOPMENT_ROADMAP.md` 与 `docs/4-tasks/CURRENT_PLAN.md`，记录 TASK-009 完成状态、实际验证结果与后续 TASK-010 入口

## Dependencies

- T5 至 T10 依赖 T1 至 T4 的需求与契约；T12 至 T15 依赖 T3、T4 及对应后端接口。
- T8 的预警处理必须使用数据库条件更新抵御重复和并发操作，但不得扩展现有表结构。
- T9 必须复用 TASK-007 已有调度服务与日志，不得新增平行超时处置流程。
- T11、T16 至 T20 依赖全部实现任务完成。

## Blockers

<!-- Fill in if something is preventing progress -->
