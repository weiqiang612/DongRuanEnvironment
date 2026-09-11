# TASK-007: NEPM 管理者工作台与任务调度

**Status**: 功能开发完成（浏览器人工验收待补）
**Created**: 2026-09-11
**Feature dir**: `docs/4-tasks/features/TASK-007-nepm-dispatch/`

## Objective

让已登录的系统管理员在真实公众反馈基础上完成查询、调度和超时处置，为网格员端提供可消费的已指派任务。

## Scope

### In scope

- 将 `/nepm/portal` 升级为 NEPM 管理者工作台，提供待办数量、近期反馈和超时优先事项。
- 提供公众反馈列表与详情，支持按省、市、主状态、超时标识和提交时间查询；列表和详情展示当前指派对象及指派日志。
- 为待指派反馈提供候选网格员：仅返回 `grid_member.state = 0` 的人员，按同城市优先、同省其他城市兜底的顺序展示；同省无候选人时明确提示不可指派。
- 管理员可对待指派反馈首次指派；对已指派或已超时反馈重派；选择当前网格员时仅记录继续处理。指派更新与日志写入必须在同一服务层事务中完成。
- 提供超时预警视图，展示待指派超过 2 小时、已指派超过 24 小时仍未完成的反馈；超时不阻断重派或继续处理。
- 提供仅基于反馈与调度状态的运营概览，不展示或伪造检测结果、最终 AQI、AQI 预警或决策统计。
- 工作台侧栏保留“检测结果”等后续模块入口并明确标注“后续开放”；不渲染模拟业务数据。
- 服务端从 HTTP Session 确认 `NEPM_ADMIN` 身份并解析实际操作管理员；前端不得传入或伪造操作人、状态、超时标识或指派日志。
- 同步需求分析、架构、接口与 UI 文档，补充后端测试、前端质量门禁和 Chrome 验收。

### Out of scope

- 不实现网格员任务列表、实测数据提交、最终 AQI 计算、检测结果查询、高等级 AQI 预警、预警处置或完整统计图表；这些内容分别留待 TASK-008、TASK-009 和 TASK-010。
- 不接入东软 HR 实时工作状态；本任务仅以本地 `grid_member.state = 0` 判断网格员可工作。
- 不新增或修改表、字段、索引、约束、数据迁移及状态主语义；不新增物理外键。
- 不引入 JWT、全局拦截器、Pinia 持久化、第三方 UI 库或新运行时依赖。

## Acceptance criteria

```json
[
  {
    "id": "AC-001",
    "category": "functional",
    "description": "已登录 NEPM 管理员可在工作台查询真实反馈、查看详情和指派日志，并按省、市、状态、超时标识和提交时间筛选。",
    "steps": [
      "以 NEPM_ADMIN 身份登录并打开 /nepm/portal。",
      "查看工作台待办概览，使用反馈列表的各项筛选条件并打开一条反馈详情。",
      "验证列表、详情和运营概览均基于现有反馈与调度数据，详情显示当前状态、指派对象和指派日志。"
    ],
    "passes": false
  },
  {
    "id": "AC-002",
    "category": "functional",
    "description": "管理员可按同城优先、同省兜底规则选择可工作网格员，并完成首次指派、重派或继续处理的可追溯操作。",
    "steps": [
      "准备同城可工作、仅同省异地可工作和同省无可工作网格员三种反馈场景。",
      "分别查询候选人并对待指派、已指派或已超时反馈执行首次指派、重派和选择原网格员继续处理。",
      "验证候选顺序、空候选提示、反馈当前指派信息与 ASSIGN、REASSIGN、CONTINUE 日志均符合规则。"
    ],
    "passes": false
  },
  {
    "id": "AC-003",
    "category": "edge-case",
    "description": "超时预警视图正确展示待指派超过 2 小时及已指派超过 24 小时未完成的反馈，且管理员仍可重派或继续处理。",
    "steps": [
      "准备待指派超过 2 小时、已指派超过 24 小时以及未超时的反馈。",
      "打开超时预警视图并对两条超时反馈分别执行可用的调度操作。",
      "验证超时事项被优先展示，未超时事项不被误标；操作后主状态与超时标识保持符合既有状态语义。"
    ],
    "passes": false
  },
  {
    "id": "AC-004",
    "category": "security",
    "description": "管理端调度权限、操作人、候选人可工作性和反馈状态限制均由服务端强制执行。",
    "steps": [
      "以未登录、NEPS、NEPG、NEPV 或伪造员工 Session 调用管理端查询与调度接口。",
      "尝试向指派请求传入操作人、状态、超时标识或指派日志字段，并尝试指派不可工作网格员、已完成反馈或已发生状态变化的反馈。",
      "验证非 NEPM_ADMIN 请求被拒绝，非法字段不影响服务端事实，失败操作不改变反馈或新增错误日志。"
    ],
    "passes": false
  },
  {
    "id": "AC-005",
    "category": "integration",
    "description": "管理端反馈、候选人、指派、重派、继续处理和超时查询接口与更新后的 ResultVO、Session、逻辑关联和状态契约一致。",
    "steps": [
      "核对接口请求不含操作人、状态、超时标识或指派日志等服务端字段，且所有响应保持 ResultVO 结构。",
      "核对服务层从 Session 的管理员业务编号解析 admins.admin_id，并在同一事务内更新 aqi_feedback 与 task_assign_log。",
      "核对候选网格员、反馈、管理员和日志的关联均由服务层校验，未新增数据库结构或迁移。"
    ],
    "passes": false
  },
  {
    "id": "AC-UI-UX",
    "category": "integration",
    "description": "Chrome 验证 NEPM 工作台及调度页面在桌面和移动尺寸下可用，后续模块标识清晰，且控制台无 JavaScript 错误。",
    "steps": [
      "在 1440x900 打开 /nepm/portal、/nepm/feedbacks、/nepm/dispatch、/nepm/timeout-alerts 和 /nepm/analytics，验证工作台、筛选区、列表、详情、调度操作、超时事项、运营概览及后续开放入口的布局、间距和溢出正常，并存档 TASK-007-nepm-dispatch-desktop.png。",
      "在 375x812 打开相同页面，验证侧栏或导航、筛选、列表、详情和主操作可见、可点击且无裁切或重叠，并存档 TASK-007-nepm-dispatch-mobile.png。",
      "悬停筛选重置、详情、指派、重派、继续处理、超时事项和后续开放入口，验证对应视觉反馈、确认提示或不可用说明。",
      "审计整个流程的浏览器控制台，验证零 JavaScript 错误。"
    ],
    "passes": false
  }
]
```

## Notes

### Documentation impact

| Area | Impacted | Maintenance target |
|---|---:|---|
| requirements | true | `docs/1-requirements/requirements_analysis.md` |
| architecture | true | `docs/2-designs/architecture.md` |
| api | true | `docs/2-designs/api_contract.md` |
| db | false | 复用既有 `aqi_feedback`、`grid_member`、`admins` 与 `task_assign_log`，不改结构。 |
| ui | true | `docs/2-designs/ui_prototype.md` |
| constraints | false | 既有 Session、事务、状态和 API/数据库变更边界足以覆盖。 |
| adr | false | 不引入新的身份来源、持久化方案或架构模式。 |
| agent-runtime | false | 端口、依赖、配置键和启动方式不变。 |

### Approval-sensitive changes

- 管理端将新增反馈查询与任务调度 API，并将 `/nepm/portal` 从最小落地页升级为工作台；用户已于 2026-09-11 确认本任务范围与调度规则。
- 不新增数据库迁移、依赖、环境变量或运行时配置。

### Explicit non-maintenance

- `docs/1-requirements/project_overview.md`、`docs/2-designs/db_schema.md`、根目录和模块 `AGENTS.md`、启动脚本及 `.codex/` 不维护，因为项目目标、物理表结构、运行方式和质量门禁不改变。

### Completion note

- 2026-09-11：功能代码、接口、后端自动测试（23 项）以及前端类型检查、构建和 Lint 均已完成并通过。由于浏览器连接初始化异常，AC-UI-UX 的桌面、移动端、悬停和控制台人工验收尚未执行，保留为后续补验项；其余功能将以代码与自动测试结果作为当前收尾依据。
