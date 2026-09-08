# Always Do

## Session and task work

- ✅ 每次会话先阅读 `docs/4-tasks/CURRENT_PLAN.md` 和活动任务的 `spec.md`。
- ✅ 非简单改动前检查 `never-do.md`；环境未启动时仅报告状态，等待明确的人为启动决定。
- ✅ 变更 API 或数据库前阅读并同步 `docs/2-designs/api_contract.md` 与 `docs/2-designs/db_schema.md`。

## Quality

- ✅ 后端改动完成前运行 `cd back/demo && ./mvnw test`。
- ✅ 前端改动完成前运行 `cd front && npm run build` 与 `npm run lint`。
- ✅ 前端遵守两空格、单引号、无分号、100 字符行宽的现有格式化规则。
- ✅ 完成后更新任务 `tasks.md` 与 `CURRENT_PLAN.md`，并记录实际验证结果。
