# TASK-009: NEPM 管理者端检测处置与运营统计

**Status**: Ready
**Created**: 2026-09-12
**Feature dir**: `docs/4-tasks/features/TASK-009-nepm-detection/`

## Objective

让已登录的 NEPM 管理员基于 TASK-008 产生的真实检测结果查看检测明细、处理高等级 AQI 预警和既有超时任务，并查看管理运营统计，为后续 NEPV 决策大屏提供可信数据基础。

## Scope

### In scope

- 提供检测结果分页查询与详情，展示关联反馈、地区、网格员、三项实测值及等级、最终 AQI 和检测时间；结果只读，不增加人工确认状态或修改流程。
- 提供最终 AQI 为 4 至 6 级的预警查询与详情，并仅使用既有 `alert_record.alert_status`、`handled_at` 完成 `PENDING` 到 `HANDLED` 的处置。
- 保留并完善既有超时任务查询和重派、继续处理能力；超时不创建 `alert_record`，处置仍通过 `task_assign_log` 留痕。
- 提供基于有效完成检测结果的管理统计，包括检测数量、AQI 等级分布、月度趋势及高等级预警数量；省、市与时间作为查询筛选条件。
- 服务端从 HTTP Session 校验 `NEPM_ADMIN` 并解析管理员身份；所有接口保持现有 `ResultVO`、分页和错误响应边界。
- 将现有 NEPM 检测结果、AQI 预警、超时任务和统计分析占位视图接入真实接口，不展示原型模拟数据。

### Out of scope

- 不新增数据库表、字段、索引、约束或迁移，不新增检测结果确认状态、确认人、处置人、处置意见、附件或统一处置记录。
- 不允许管理员修改、驳回或要求重新提交网格员检测结果，不改变服务端 AQI 自动计算规则及 `aqi_feedback` 三态主流程。
- 不实现 NEPV 决策大屏、网格覆盖率决策指标、地图调度、消息通知、拍照取证、复杂审批流或新的业务角色。
- 不接入东软 HR，不引入 JWT、全局拦截器、新运行时依赖或新的前端状态框架。
- 不建立或维护重复统计汇总表；统计继续直接聚合 `detection_result`、`aqi_feedback` 和 `alert_record`。

## Acceptance criteria

```json
[
  {
    "id": "AC-001",
    "category": "functional",
    "description": "已登录 NEPM 管理员可查询和查看真实检测结果，且检测事实保持只读。",
    "steps": [
      "准备多地区、多 AQI 等级和不同检测时间的已完成检测结果。",
      "按省、市、AQI 等级、检测时间和关键词查询检测结果并打开详情。",
      "验证分页总数、关联反馈、网格员、三项实测值及等级、最终 AQI 和检测时间准确，页面不存在修改、驳回或人工确认状态。"
    ],
    "passes": true
  },
  {
    "id": "AC-002",
    "category": "functional",
    "description": "管理员可查询并处理既有高等级 AQI 预警，且处置只改变现有状态和处置时间。",
    "steps": [
      "查询最终 AQI 为 4 至 6 级的 PENDING 和 HANDLED 预警并查看详情。",
      "对一条 PENDING 预警执行处理操作。",
      "验证该记录仅从 PENDING 变为 HANDLED 并写入 handled_at；重复处理返回状态冲突且不产生重复记录。"
    ],
    "passes": true
  },
  {
    "id": "AC-003",
    "category": "edge-case",
    "description": "超时任务继续通过既有重派或继续处理能力闭环，不与 AQI 预警混淆。",
    "steps": [
      "分别准备待指派超时、已指派超时和已完成但保留历史超时标识的反馈。",
      "在超时视图核对状态，并对可操作任务执行重派或继续处理。",
      "验证操作复用 TASK-007 调度契约和 task_assign_log，已完成任务只读，且任何超时任务都不新增 alert_record。"
    ],
    "passes": true
  },
  {
    "id": "AC-004",
    "category": "functional",
    "description": "管理统计仅基于有效完成的真实检测结果，并按统一口径返回检测、AQI 趋势和预警数据。",
    "steps": [
      "准备不同省市、月份、AQI 等级及预警状态的检测结果，并保留没有检测结果的反馈。",
      "按地区和时间范围查询检测数量、AQI 等级分布、月度趋势和高等级预警数量。",
      "验证各分组之和与筛选后的 detection_result 明细一致，没有检测结果的反馈不进入检测统计，也不写入汇总表。"
    ],
    "passes": true
  },
  {
    "id": "AC-005",
    "category": "security",
    "description": "检测、预警、超时和统计接口遵守 NEPM Session、ResultVO、参数校验与并发一致性边界。",
    "steps": [
      "分别以未登录、NEPG、NEPV 和有效 NEPM Session 调用新增管理接口。",
      "提交非法分页、日期、AQI 等级、预警状态和不存在的资源编号，并并发处理同一预警。",
      "验证未登录返回 401，角色不符返回 403，非法参数返回 400，不存在返回 404，状态冲突返回 409，且响应不泄露 Session、密码或内部异常。"
    ],
    "passes": true
  },
  {
    "id": "AC-UI-UX",
    "category": "integration",
    "description": "Chrome MCP 验证 NEPM 检测结果、AQI 预警、超时处置和统计视图在桌面与移动尺寸下可用且控制台状态干净。",
    "steps": [
      "在 Chrome MCP 以 1440x900 打开 /nepm/portal，依次验证检测结果、AQI 预警、超时预警和统计分析视图的布局、筛选、分页、详情及处理反馈。",
      "切换到 375x812，验证侧栏导航、筛选区域、表格或卡片、详情和主要操作无裁切、重叠或不可达。",
      "悬停导航项、筛选控件、列表操作、分页和预警处理按钮，验证视觉反馈明确。",
      "审计完整流程的浏览器控制台，确认 JavaScript 错误为零且页面不展示模拟业务数据。",
      "存档 TASK-009-nepm-detection-desktop.png 与 TASK-009-nepm-detection-mobile.png。"
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
| db | false | 复用 `detection_result`、`aqi_feedback`、`alert_record` 与 `task_assign_log`，不改结构。 |
| ui | true | `docs/2-designs/ui_prototype.md` |
| constraints | false | 既有 Session、事务、API 与数据库审批边界足以覆盖。 |
| adr | false | 不引入新持久化方案、依赖、身份来源或架构模式。 |
| agent-runtime | false | 端口、配置、启动命令、测试和 Lint 命令不变。 |

### Approval-sensitive changes

- None。本任务已确认不新增数据库结构、依赖、状态主流程或新业务流程。

### Explicit non-maintenance

- `docs/2-designs/db_schema.md` 不修改：现有字段已覆盖检测结果、AQI 预警状态、处置时间、超时标识和调度日志。
- `docs/3-constraints/` 与 ADR 不修改：本任务没有形成新的长期约束或架构决策。
- 根目录及子模块 `AGENTS.md`、`.codex` Hook 和初始化脚本不修改：工程运行方式不变。
