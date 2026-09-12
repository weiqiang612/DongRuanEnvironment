# TASK-008: NEPG 网格员任务处理与实测提交

**Status**: Completed
**Created**: 2026-09-11
**Feature dir**: `docs/4-tasks/features/TASK-008-nepg-measurement/`

## Objective

让已登录的 AQI 检测网格员查看本人已指派任务、提交三项实测浓度，并由服务端可信地计算最终 AQI、完成任务和生成高等级预警，为后续管理处置与统计提供真实数据。

## Scope

### In scope

- 将 `/nepg/portal` 升级为网格员“我的任务”入口，并提供任务列表 `/nepg/tasks` 与任务详情 `/nepg/tasks/{afId}`；仅展示当前 NEPG Session 对应网格员的已指派和已完成任务。
- 任务详情展示反馈地区、地址、描述、公众预估 AQI、指派时间、当前状态和超时标识；已完成任务额外展示系统计算的最终 AQI、三项实测值、检测时间及预警生成结果。
- 网格员可对“当前指派给本人且 `state = 1`”的任务提交 SO₂、CO、PM2.5 实测浓度；已超时但仍为已指派的任务可继续提交。
- 新增 `POST /nepg/tasks/{afId}/measurements`。请求只接受 `so2Value`、`coValue`、`spmValue`；服务端从 NEPG HTTP Session 解析网格员身份，并在成功提交时写入检测时间。
- 服务端根据 `aqi` 字典表的 SO₂、CO、PM2.5 区间计算三个污染物等级，最终 AQI 取三者最大等级；边界值按字典区间匹配，任一浓度无法唯一匹配有效等级时拒绝提交。
- 在同一服务层事务中写入 `detection_result`、将 `aqi_feedback.state` 更新为已完成并写入 `completed_at`；若最终 AQI 为 4 至 6，同步写入一条 `alert_record`。既有 `timeout_flag` 作为历史超时事实保留，不由网格员提交覆盖。
- 服务端拒绝未登录、非 NEPG、非本人、未指派、已完成、并发重复提交、非法字段和越界浓度；失败操作不得写入半完成检测结果、完成状态或预警。
- 同步需求、架构、接口和 UI 文档，补充后端测试、前端质量门禁和 Chrome 人工验收。

### Out of scope

- 不实现管理端检测结果确认、预警处置、超时处置或完整运营统计；留待 TASK-009。
- 不实现决策端省市统计、趋势、网格覆盖率或大屏；留待 TASK-010。
- 不新增或修改表、字段、索引、约束、数据库迁移及物理外键；复用既有 `detection_result` 的一反馈一结果唯一约束和 `alert_record` 的一结果一预警唯一约束。
- 不接入检测仪器、定位、图片取证、实时轨迹或东软 HR；不引入 JWT、全局拦截器、Pinia 持久化、第三方 UI 库或新运行时依赖。

## Acceptance criteria

```json
[
  {
    "id": "AC-001",
    "category": "functional",
    "description": "已登录 NEPG 网格员可查看仅属于本人的已指派和已完成任务，并在详情中区分公众预估 AQI 与系统最终 AQI。",
    "steps": [
      "以 NEPG 网格员身份登录并打开 /nepg/tasks，准备本人已指派、本人已完成和其他网格员任务。",
      "筛选或查看任务列表并打开一条已指派任务和一条已完成任务详情。",
      "验证列表不泄露他人任务；详情准确展示反馈、指派、状态和超时信息，已完成任务还展示最终 AQI、三项实测值、检测时间及预警结果。"
    ],
    "passes": true
  },
  {
    "id": "AC-002",
    "category": "functional",
    "description": "网格员提交三项有效实测浓度后，服务端按 AQI 字典计算最终等级，在同一事务内完成任务，并在等级为 4 至 6 时生成预警。",
    "steps": [
      "准备一条当前指派给该网格员的任务，提交三个分别落入已配置 AQI 区间的浓度。",
      "验证响应和任务详情中的三个污染物等级、最终 AQI 与三者最大等级一致，检测时间由服务端写入。",
      "验证检测结果存在、反馈状态变为已完成且写入完成时间；最终 AQI 为 4 至 6 时恰好生成一条关联预警，否则不生成预警。"
    ],
    "passes": true
  },
  {
    "id": "AC-003",
    "category": "edge-case",
    "description": "边界、越界和并发重复提交均保持任务与检测数据的一致性。",
    "steps": [
      "分别提交恰好位于 AQI 字典区间边界的浓度、负数或无法匹配字典区间的浓度，并对同一任务重复或并发提交。",
      "验证边界值按照唯一对应的字典等级计算；无匹配浓度返回 HTTP 400；已完成或竞争失败的提交返回 HTTP 409。",
      "验证每条反馈最多只有一条检测结果和一条关联预警，失败提交不改变完成状态或写入部分数据。"
    ],
    "passes": true
  },
  {
    "id": "AC-004",
    "category": "security",
    "description": "NEPG 任务查询与实测提交的身份、任务归属和服务端事实均不可由客户端伪造。",
    "steps": [
      "以未登录、NEPS、NEPM、NEPV 和另一名 NEPG 网格员身份调用任务查询、详情和提交接口。",
      "尝试在提交请求中附带网格员编号、最终 AQI、污染物等级、状态、检测时间、预警或完成时间字段，并尝试提交未指派或不属于本人的任务。",
      "验证未登录请求返回 HTTP 401，越权请求返回 HTTP 403，状态冲突返回 HTTP 409；客户端附加字段不改变任何服务端事实。"
    ],
    "passes": true
  },
  {
    "id": "AC-005",
    "category": "integration",
    "description": "NEPG 任务与实测接口保持 ResultVO、Session、AQI 字典、检测结果、反馈状态和预警记录的既有契约一致。",
    "steps": [
      "核对任务列表、详情和实测提交接口的请求、响应和错误码均符合更新后的 api_contract.md。",
      "核对服务端从 employeeRole = NEPG_GRID_MEMBER 与 employeeAccountCode 解析实际 gm_id，前端不传入该身份。",
      "核对 AQI 计算读取 aqi 表，事务内复用 detection_result、aqi_feedback 和 alert_record 的现有逻辑关联与唯一约束，未修改数据库结构。"
    ],
    "passes": true
  },
  {
    "id": "AC-UI-UX",
    "category": "integration",
    "description": "Chrome 验证 NEPG 任务列表、详情和实测提交在桌面与移动尺寸下可用，交互反馈清晰且控制台无 JavaScript 错误。",
    "steps": [
      "在 1440x900 打开 /nepg/tasks 和任务详情，验证任务状态、超时标识、实测表单、完成结果、错误提示和提交结果布局正常，并存档 TASK-008-nepg-measurement-desktop.png。",
      "在 375x812 打开相同流程，验证列表、详情、三项输入、提交按钮和结果卡片可见、可操作且无裁切或重叠，并存档 TASK-008-nepg-measurement-mobile.png。",
      "悬停任务行、详情入口、提交按钮和已完成任务的只读结果区域，验证相应视觉反馈、禁用说明或可理解提示。",
      "审计整个流程的浏览器控制台，验证零 JavaScript 错误。"
    ],
    "passes": true
  }
]
```

## Notes

### Documentation impact

| Area | Impacted | Maintenance target |
|---|---:|---|
| requirements | true | `docs/1-requirements/project_overview.md`、`docs/1-requirements/requirements_analysis.md` |
| architecture | true | `docs/2-designs/architecture.md` |
| api | true | `docs/2-designs/api_contract.md` |
| db | false | 复用 `aqi_feedback`、`aqi`、`detection_result`、`alert_record` 与现有逻辑关联，不改结构。 |
| ui | true | `docs/2-designs/ui_prototype.md` |
| constraints | false | 既有 Session、事务、API/数据库契约和先确认边界足以覆盖。 |
| adr | false | 不引入新的依赖、身份来源、持久化方案或架构模式。 |
| agent-runtime | false | 端口、依赖、配置键、启动方式和质量门禁不变。 |

### Approval-sensitive changes

- 将新增 NEPG 任务查询与实测提交 API，并将 `/nepg/portal` 升级为任务入口；用户已于 2026-09-11 确认范围、服务端检测时间和实施方案。
- 不新增数据库迁移、依赖、环境变量或运行时配置。

### Explicit non-maintenance

- `docs/2-designs/db_schema.md`、根目录和模块 `AGENTS.md`、启动脚本及 `.codex/` 不维护，因为物理表结构、运行方式和质量门禁不改变。
