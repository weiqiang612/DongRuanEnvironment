# TASK-001: Tasks

**Spec**: `spec.md`
**Status**: Complete

## Key decisions

- `aqi_feedback` 继续承载反馈和任务主状态，不新增 `inspection_task`。
- 空的 `statistics` 重构为 `detection_result`，统计通过查询聚合而非重复汇总表实现。
- 超时是任务标识；`alert_record` 仅记录最终 AQI 大于等于 4 的预警。
- 全部跨表关联采用逻辑外键；保留关联字段和索引，不建立 MySQL `FOREIGN KEY`。
- `submitted_at` 默认当前时间，保证仅传旧日期时间字段的既有插入接口仍可执行。

## Progress

- [x] T1 — 更新 `docs/2-designs/db_schema.md` 和版本化迁移设计 · covers: AC-001, AC-002, AC-003
- [x] T2 — 读取本地数据库结构和迁移前置条件，创建受影响表备份 · covers: AC-003, AC-004
- [x] T3 — 执行正向迁移并核验表、字段、索引和关联 · covers: AC-001, AC-002, AC-004
- [x] T4 — 执行后端测试，验证当前反馈基线未因迁移失效 · covers: AC-001
- [x] T5 — 验证 AC 并更新 `spec.md` 的通过状态 · covers: AC-001, AC-002, AC-003, AC-004
- [x] T6 — 更新 `docs/4-tasks/CURRENT_PLAN.md`，标记任务完成。
- [x] T7 — 移除全部物理外键、修复中文注释编码并完成复核 · covers: AC-002, AC-004
- [x] T8 — 修复旧反馈插入对 `submitted_at` 必填字段的兼容性并回归测试 · covers: AC-001

## Dependencies

- T2 requires T1.
- T3 requires T2.
- T4 and T5 require T3.
- T6 requires T5.
- T7 requires T3.
- T8 requires T3.

## Blockers

<!-- Fill in if something is preventing progress -->
