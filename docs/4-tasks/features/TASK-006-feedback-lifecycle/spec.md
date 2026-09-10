# TASK-006: NEPS 反馈归属与生命周期

**Status**: In Progress
**Created**: 2026-09-10
**Feature dir**: `docs/4-tasks/features/TASK-006-feedback-lifecycle/`

## Objective

让已登录的公众监督员只能创建、查看和维护本人的反馈，并以待指派、已指派、已完成、已超时状态展示可追溯的处理进度。

## Scope

### In scope

- 将 `/aqiFeedback` 升级为 NEPS 公众工作台，包含提交反馈与我的反馈列表；列表仅返回当前登录公众的记录。
- 提交反馈时服务端从 NEPS HTTP Session 取得 `telId` 并写入归属；前端请求不再传入、覆盖或伪造 `telId`。
- 公众可查看自己的反馈详情及待指派、已指派、已完成、已超时展示状态。
- 仅反馈归属人可在主状态为待指派时编辑或删除；已指派、已完成或带有超时标识的反馈只读。
- 未登录、非归属人或不满足可编辑状态的请求由服务端拒绝；前端仅根据服务端结果展示提示。
- 工作台顶部提供“欢迎您”账户菜单和退出登录操作；退出后销毁当前 NEPS Session 并返回统一登录入口。
- 同步需求、架构、接口与 UI 文档，补充后端测试、前端质量门禁和 Chrome 验收。

### Out of scope

- 不实现注册、找回密码、JWT、全局拦截器或新的 Session 持久化方案。
- 不改变 `aqi_feedback` 的表结构、主状态语义或既有 `state`、`timeout_flag` 维护责任；仅补齐上海市、重庆市的直辖市城市基础数据迁移。
- 不实现管理员指派、网格员检测、预警超时计算、统计或决策大屏。
- 不改变现有删除接口的 HTTP 方法；方法调整留待兼容性评估后的独立任务。

## Acceptance criteria

```json
[
  {
    "id": "AC-001",
    "category": "functional",
    "description": "已登录公众可在工作台提交反馈，服务端以当前 NEPS Session 身份写入归属，并且我的反馈列表与详情仅显示本人记录和处理状态。",
    "steps": [
      "以公众监督员身份登录后打开 /aqiFeedback，提交不含 telId 的有效反馈请求。",
      "查询我的反馈列表和其中一条详情。",
      "验证记录归属于当前登录手机号，展示待指派、已指派、已完成或已超时状态，且响应不包含其他公众的记录。"
    ],
    "passes": false
  },
  {
    "id": "AC-002",
    "category": "edge-case",
    "description": "归属人只能编辑或删除待指派反馈；已指派、已完成或已超时反馈保持只读。",
    "steps": [
      "准备同一公众名下的待指派、已指派、已完成和已超时反馈。",
      "对待指派记录执行编辑与删除，对其余记录重复执行相同操作。",
      "验证待指派操作成功，其余操作被服务端拒绝且原记录和状态未被修改。"
    ],
    "passes": false
  },
  {
    "id": "AC-003",
    "category": "security",
    "description": "反馈归属和状态限制在服务端强制执行，未登录或越权请求不能读取、修改或删除他人反馈。",
    "steps": [
      "分别以未登录状态和另一公众的 Session 调用我的反馈、详情、编辑、删除接口，并尝试在提交或编辑请求中伪造 telId。",
      "验证未登录请求返回认证失败；非归属人或不可编辑状态请求被拒绝且不泄露其他公众的反馈内容。",
      "验证服务端忽略或拒绝前端传入的 telId，归属始终来自当前 Session。"
    ],
    "passes": false
  },
  {
    "id": "AC-004",
    "category": "integration",
    "description": "Session 归属、反馈 API、前端请求和现有 aqi_feedback 状态字段与更新后的接口和架构契约一致。",
    "steps": [
      "核对新增或调整的反馈请求不再将 telId 作为可写前端字段，并保持 ResultVO 响应结构。",
      "核对服务层复用现有 aqi_feedback 的 tel_id、state 和 timeout_flag，不新增表结构或迁移。",
      "验证前端 API 路径、工作台路由和错误提示与 api_contract.md、architecture.md 一致。"
    ],
    "passes": false
  },
  {
    "id": "AC-UI-UX",
    "category": "integration",
    "description": "Chrome 验证公众工作台在桌面和移动尺寸下可用，状态与可编辑性清晰，且控制台无 JavaScript 错误。",
    "steps": [
      "在 1440x900 打开 /aqiFeedback，验证提交表单、我的反馈列表、状态标签与详情或编辑操作布局、间距和溢出正常，并存档 TASK-006-feedback-desktop.png。",
      "在 375x812 打开相同页面，验证表单、列表、导航和主操作可见、可点击且无裁切或重叠，并存档 TASK-006-feedback-mobile.png。",
      "悬停提交按钮、待指派反馈的编辑和删除操作，以及只读状态反馈，验证对应的视觉反馈或禁用提示。",
      "审计浏览器控制台，验证整个流程零 JavaScript 错误。"
    ],
    "passes": false
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
| db | true | `docs/2-designs/db_schema.md`、`V20260910_006__municipality_city_options.sql` 与回滚脚本。 |
| ui | true | `docs/2-designs/ui_prototype.md` |
| constraints | false | 既有 Session、接口、状态与审批边界足以覆盖。 |
| adr | false | 不引入新的架构决策或身份来源。 |
| agent-runtime | false | 端口、依赖、配置键和启动方式不变。 |

### Approval-sensitive changes

- 反馈接口将收紧为基于 Session 的归属校验，且待指派以外状态不允许公众编辑或删除；该产品规则已由用户于 2026-09-10 确认。
- 直辖市城市基础数据迁移执行前需备份 `grid_city` 与相关 `aqi_feedback` 数据；不新增表、字段、依赖、环境变量或运行时配置。

### Explicit non-maintenance

- `docs/2-designs/db_schema.md`、根目录和模块 `AGENTS.md`、启动脚本及 `.codex/` 不维护，因为表结构、运行方式和质量门禁不改变。
