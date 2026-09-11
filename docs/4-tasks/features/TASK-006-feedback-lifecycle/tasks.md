# TASK-006: Tasks

**Spec**: `spec.md`
**Status**: Completed

## Key decisions

- 以 NEPS HTTP Session 中的 `telId` 作为唯一反馈归属来源，前端不得写入或覆盖 `telId`。
- 复用 `aqi_feedback` 既有 `tel_id`、`state`、`timeout_flag` 与时间字段；不新增表、字段或迁移。
- 公众仅在主状态为待指派且未超时时编辑或删除本人反馈；后续状态只读。
- `/aqiFeedback` 保持现有地址并升级为公众工作台，不新建平行入口。
- 仍复用 `ResultVO`、最小 HTTP Session 和现有删除接口方法；不引入 JWT、全局拦截器或依赖。

## Progress

- [x] T1 — 更新 `project_overview.md` 与 `requirements_analysis.md`，明确公众反馈归属、可编辑状态与本任务非目标 · covers: doc-maintenance, AC-001, AC-002, AC-003
- [x] T2 — 更新 `api_contract.md` 与 `architecture.md`，定义 Session 归属的我的反馈查询、详情、保存、更新和删除契约及服务层边界 · covers: AC-001, AC-002, AC-003, AC-004
- [x] T3 — 补齐反馈归属与生命周期所需 DTO、查询条件、Mapper 映射和状态展示数据对象，复用既有 aqi_feedback 字段 · covers: AC-001, AC-002, AC-004
- [x] T4 — 实现服务层的 Session 归属校验、我的反馈查询、待指派可编辑性校验和写操作保护 · covers: AC-001, AC-002, AC-003, AC-004
- [x] T5 — 实现或调整反馈 Controller，使保存、查询、详情、更新和删除统一经服务端 Session 与 ResultVO 边界处理 · covers: AC-001, AC-002, AC-003, AC-004
- [x] T6 — 新增或调整前端反馈 API 客户端，移除可写 telId 并映射我的反馈、详情及安全失败提示 · covers: AC-001, AC-003, AC-004
- [x] T7 — 升级 `/aqiFeedback` 为公众工作台，完成提交、我的反馈列表、状态标签、详情和待指派编辑/删除交互 · covers: AC-001, AC-002, AC-UI-UX
- [x] T8 — 补充后端测试，覆盖 Session 归属、未登录、越权、伪造 telId、待指派编辑删除与其他状态只读 · covers: AC-001, AC-002, AC-003, AC-004
- [x] T9 — 运行 `cd back && ./mvnw test` · covers: AC-001, AC-002, AC-003, AC-004
- [x] T10 — 运行 `cd front && npm run build` 与 `cd front && npm run lint` · covers: AC-001, AC-UI-UX
- [x] T10a — 执行前备份受影响基础数据，并运行 `V20260910_006__municipality_city_options.sql`，验证上海市、重庆市各返回一个城市选项 · covers: AC-UI-UX
- [x] T11 — 使用 Chrome 完成 `/aqiFeedback` 的 1440x900、375x812、悬停、控制台和命名截图验收 · covers: AC-UI-UX
- [x] T12 — 验证 AC，并更新 `spec.md` 的通过状态 · covers: AC-001, AC-002, AC-003, AC-004, AC-UI-UX
- [x] T13 — 更新 `docs/4-tasks/CURRENT_PLAN.md`，标记任务完成并记录实际验证结果 · covers: doc-maintenance

## Dependencies

- T2 依赖 T1；T3 至 T5 依赖 T2；T6、T7 依赖 T2 和 T5。
- T8 依赖 T3 至 T5；T9 至 T13 依赖全部实现和验收任务。

## Completion record

- 2026-09-11：用户确认已完成人工验收；T10a 至 T13 按该确认收尾。
