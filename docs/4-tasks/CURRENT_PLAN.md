# Current Plan

> 每次会话先阅读本文件。

## Active feature

无。下一项为 TASK-008 网格员端任务处理与实测提交，启动前需创建独立规格与任务清单。

## Stages

### TASK-007: NEPM 管理者工作台与任务调度
- [x] 功能开发完成（浏览器人工验收延后）

### TASK-006: NEPS 反馈归属与生命周期
- [x] Implementation (13 tasks)

### TASK-005: NEPG、NEPM 与 NEPV 员工真实登录
- [x] Implementation (15 tasks)

### TASK-004: NEPS 公众监督员登录 MVP
- [x] Implementation (14 tasks)

### TASK-003: 前后端代码基线重构
- [x] Implementation (11 tasks；浏览器验收由用户明确暂缓)

### TASK-002: 后端目录与包名迁移收尾
- [x] Implementation (7 tasks)

### TASK-001: 数据库迁移与反馈检测闭环
- [x] Implementation (8 tasks)

## Completed

- TASK-007 NEPM 管理者工作台与任务调度（2026-09-11）：完成真实反馈查询、详情、同城优先/同省兜底候选人、首次指派/重派/继续处理、指派日志、超时筛选、运营概览及管理员默认工作台；后端 23 项测试、前端类型检查、构建和 Lint 通过。Chrome 桌面/移动端及控制台人工验收因浏览器连接初始化异常延后，不阻塞进入 TASK-008。
- TASK-005 NEPG、NEPM 与 NEPV 员工真实登录（2026-09-11）：完成三端员工认证、PBKDF2 兼容迁移、角色隔离、统一登录视图与最小落地页；用户确认已完成人工验收，AC-001 至 AC-UI-UX 均标记通过。
- TASK-006 NEPS 反馈归属与生命周期（2026-09-11）：已完成 Session 归属保护、公众反馈工作台、状态只读规则、后端测试、前端构建与 Lint；用户确认已完成人工验收，AC-001 至 AC-UI-UX 均标记通过。
- TASK-004 NEPS 公众监督员登录 MVP（2026-09-08）：已完成系统统一入口（PC 响应式四端卡片）、NEPS 登录页（自适应与素材复用）、PBKDF2 密码哈希与平滑升级、认证会话与跳转 `/aqiFeedback`；后端测试（10/10 通过）、前端构建与 Lint（0 error 0 warning）通过；Chrome 1440x900 / 1366x768 桌面视口验收通过，消除硬编码尺寸与垂直滚动条，零控制台错误，已存档验收截图。
- TASK-003 前后端代码基线重构（2026-09-08）：已完成接口契约、后端分层与校验、实体映射、脚手架清理、前端反馈页与构建检查；后端测试、前端构建和 Lint 通过。浏览器桌面/移动端验收由用户明确暂缓，未作为完成阻塞项。
- TASK-002 后端目录与包名迁移收尾（2026-09-08）：后端 Maven 根目录统一为 `back/`，Java 根包统一为 `com.dongruan.environment`；启动脚本、工程指引和设计路径已同步，后端测试通过。
- TASK-001 数据库迁移与反馈检测闭环（2026-09-08）：已备份本地数据库，执行 `V20260908_001`、`V20260908_002`、`V20260908_003`，将空 `statistics` 重构为 `detection_result`，移除全部物理外键并修复中文注释，兼容旧反馈插入；后端测试通过。

## Notes for next session

<!-- 记录下一次会话所需的上下文与验证结果。 -->
- 后续开发顺序与依赖见 `docs/4-tasks/DEVELOPMENT_ROADMAP.md`；下一步为 TASK-008，先创建网格员端任务列表、实测提交与服务端 AQI 计算的任务规格与实施计划。
