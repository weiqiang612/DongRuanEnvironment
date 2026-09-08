# Current Plan

> 每次会话先阅读本文件。

## Active feature

`docs/4-tasks/features/TASK-003-baseline-code-refactor/`
- spec.md ✅ Ready
- tasks.md → Not started

## Stages

### TASK-003: 前后端代码基线重构
- [ ] Implementation (11 tasks)

### TASK-002: 后端目录与包名迁移收尾
- [x] Implementation (7 tasks)

### TASK-001: 数据库迁移与反馈检测闭环
- [x] Implementation (8 tasks)

## Completed

- TASK-002 后端目录与包名迁移收尾（2026-09-08）：后端 Maven 根目录统一为 `back/`，Java 根包统一为 `com.dongruan.environment`；启动脚本、工程指引和设计路径已同步，后端测试通过。
- TASK-001 数据库迁移与反馈检测闭环（2026-09-08）：已备份本地数据库，执行 `V20260908_001`、`V20260908_002`、`V20260908_003`，将空 `statistics` 重构为 `detection_result`，移除全部物理外键并修复中文注释，兼容旧反馈插入；后端测试通过。

## Notes for next session

<!-- 记录下一次会话所需的上下文与验证结果。 -->
