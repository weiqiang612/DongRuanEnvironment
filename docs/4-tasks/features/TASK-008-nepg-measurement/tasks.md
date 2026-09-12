# TASK-008: Tasks

**Spec**: `spec.md`
**Status**: Completed

## Key decisions

- 仅复用既有 `aqi_feedback` 作为任务主表；NEPG 只能查看本人 `state = 1` 或 `state = 2` 的任务，且只可提交当前指派给本人的 `state = 1` 任务。
- 最终 AQI 不由前端计算或提交；服务端从 `aqi` 字典的三组污染物范围逐项计算等级，取最大等级，并在成功提交时写入检测时间。
- 检测结果写入、任务完成和高等级预警生成置于同一服务层事务；依赖 `detection_result.feedback_id` 和 `alert_record.result_id` 的既有唯一约束防止重复事实。
- 完成任务不篡改既有 `timeout_flag`；超时事实保留，由后续 TASK-009 统一处置和统计。

## Progress

- [x] T1 — 更新 `docs/1-requirements/project_overview.md` 与 `docs/1-requirements/requirements_analysis.md`，将网格员任务、实测、服务端 AQI 计算与预警生成从目标能力同步为本任务范围 · covers: doc-maintenance, AC-001, AC-002
- [x] T2 — 更新 `docs/2-designs/architecture.md`，定义 NEPG Session 授权、AQI 字典计算、事务完成链路和预警生成边界 · covers: doc-maintenance, AC-002, AC-004, AC-005
- [x] T3 — 更新 `docs/2-designs/api_contract.md`，定义网格员任务列表、详情与实测提交的请求/响应、ResultVO、错误码、Session 与幂等边界 · covers: AC-001, AC-002, AC-003, AC-004, AC-005
- [x] T4 — 更新 `docs/2-designs/ui_prototype.md`，定义 NEPG 任务列表、详情、实测表单、完成结果、只读状态和响应式交互 · covers: doc-maintenance, AC-001, AC-UI-UX
- [x] T5 — 新增检测结果、AQI 字典与网格员任务所需 DTO、实体、Mapper 映射和查询对象，复用既有物理表及字段 · covers: AC-001, AC-002, AC-005
- [x] T6 — 实现 NEPG 服务层 Session 身份解析、本人任务查询和详情读取，拒绝未授权或跨网格员访问 · covers: AC-001, AC-004, AC-005
- [x] T7 — 实现实测浓度校验、AQI 字典匹配、最终等级计算、检测结果写入、任务完成和条件性预警生成的原子事务 · covers: AC-002, AC-003, AC-004, AC-005
- [x] T8 — 实现 NEPG Controller 与 ResultVO 边界，提供任务列表、详情和实测提交 API，并严格忽略或拒绝服务端事实字段 · covers: AC-001, AC-002, AC-003, AC-004, AC-005
- [x] T9 — 补充后端测试，覆盖本人隔离、未登录与角色拒绝、AQI 最大等级、字典边界与无匹配值、重复提交和高等级预警 · covers: AC-001, AC-002, AC-003, AC-004, AC-005
- [x] T10 — 新增前端 NEPG API 客户端、类型和路由，将 `/nepg/portal` 迁移为 `/nepg/tasks` 的任务入口 · covers: AC-001, AC-005
- [x] T11 — 实现网格员任务列表、详情与实测提交页面，提供字段范围提示、加载与重复提交防护、成功结果、失败提示和已完成只读展示 · covers: AC-001, AC-002, AC-003, AC-UI-UX
- [x] T12 — 使用 Chrome 完成桌面 1440x900、移动 375x812、相关悬停状态、控制台审计和命名截图存档 · covers: AC-UI-UX
- [x] T13 — 运行 `cd back && ./mvnw test`，全部测试必须通过
- [x] T14 — 逐项验证 `spec.md` 的验收标准，并将通过项的 `passes` 更新为 `true`
- [x] T15 — 运行 `cd front && npm run build` 与 `cd front && npm run lint`，构建通过且无 Lint 违规
- [x] T16 — 更新 `docs/4-tasks/CURRENT_PLAN.md`，记录完成状态、实际验证结果与后续 TASK-009 入口

## Dependencies

- T5 至 T8 依赖 T2、T3；T10、T11 依赖 T3、T4、T8。
- T9 依赖 T5 至 T8；T12 至 T16 依赖全部实现与质量任务。
- T7 的检测结果、任务完成和预警写入必须始终处于同一服务层事务，不能拆分为前端串行请求。

## Blockers

<!-- Fill in if something is preventing progress -->
