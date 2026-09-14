# TASK-010: NEPV 决策端中国地图与综合分析大屏

**Status**: Completed
**Created**: 2026-09-14
**Feature dir**: `docs/4-tasks/features/TASK-010-nepv-decision/`

## Objective

让已登录的 NEPV 决策者在项目内通过中国省级地图下钻至市级地图，并结合真实统计数据识别区域环境风险、网格覆盖与高等级预警，为决策提供只读、可追溯的综合视图。

## Scope

### In scope

- 在 `/nepv/portal` 提供 NEPV 决策驾驶舱；地图使用 DataV GeoAtlas 准备的中国省级及省内市级 GeoJSON 和项目既有 ECharts，不嵌入外部 DataV 页面，也不增加运行时外部服务或密钥。
- 按省级行政区展示真实检测量、高等级污染占比和待处置 AQI 预警；点击省份后地图切换为该省的市级边界，并让本页指标、AQI 分布、趋势和城市数据表同步收窄至该省；再点击某市或城市数据行时，城市筛选自动选中该市，统计同步收窄到该市且保留省内市级地图。
- 提供省、市、提交日期范围筛选，以及 AQI 1~6 级分布、月度检测/预警趋势、网格覆盖率、高等级预警汇总和只读最新预警列表。
- 网格覆盖率固定为“筛选范围内至少配置一名 `grid_member` 的 `grid_city` 数量 ÷ 筛选范围内全部 `grid_city` 数量”；无城市时返回 `0`，不得除零或伪造覆盖率。
- 新增仅限 `NEPV_DECISION_MAKER` 的只读聚合接口；所有数据从 `detection_result`、`aqi_feedback`、`alert_record`、`grid_province`、`grid_city`、`grid_member` 实时聚合。
- 在前端维护“项目内部省份 ID/名称 → 国家行政区 adcode”映射，并在地图资源或映射无法匹配时明确显示未覆盖状态，不以错误区域着色。

### Out of scope

- 不新增数据库表、字段、索引、迁移、缓存或重复统计汇总表。
- 不接入阿里云天池、DataV 控制台页面、地图 API Key、外部地图运行时 SDK、实时定位、轨迹、地图调度或区县下钻。
- 不提供预警处置、任务指派、检测结果编辑、地图标注写入或任何 NEPM 写操作；NEPV 全程只读。
- 不改变 HTTP Session、认证方案、AQI 自动计算、预警处置状态机或既有 NEPM/NEPG/NEPS 页面。

## Acceptance criteria

```json
[
  {
    "id": "AC-001",
    "category": "functional",
    "description": "有效 NEPV 决策者可在项目内查看中国省级风险地图、下钻到省内市级地图，并按区域和时间查看真实决策统计。",
    "steps": [
      "准备至少两个省份的检测结果、不同 AQI 等级和高等级预警数据。",
      "以 NEPV Session 打开 /nepv/portal，依次按省、市和日期范围查询。",
      "验证省级地图、下钻后的市级地图和城市数据表，以及检测量、AQI 分布、月度趋势、预警汇总均与筛选后的真实记录一致，且不展示示例数据。"
    ],
    "passes": true
  },
  {
    "id": "AC-002",
    "category": "functional",
    "description": "地图悬停和省份点击下钻可读地展示并联动真实风险指标，地图资源或区域编码无法匹配时安全降级。",
    "steps": [
      "准备能够匹配中国省级和省内市级 GeoJSON 的省份统计数据，并准备一条无匹配映射的区域数据。",
      "悬停省份、点击省份、悬停市级区域、点击返回全国，再点击重置筛选。",
      "验证悬停内容包含真实检测量、高等级污染和待处置预警；省份点击后外围统计和城市表同步收窄；无匹配区域不被错误着色且有明确说明。"
    ],
    "passes": true
  },
  {
    "id": "AC-003",
    "category": "integration",
    "description": "网格覆盖率与高等级预警统计遵循已确认的实时只读口径。",
    "steps": [
      "准备有网格员、无网格员及无城市的筛选范围，并准备 PENDING 与 HANDLED 的高等级预警。",
      "分别请求 NEPV 决策统计接口并查看页面。",
      "验证覆盖率等于有至少一名网格员的城市数除以全部城市数；分母为零时为 0；预警总数、待处置数、已处置数和最新预警与 alert_record 一致。"
    ],
    "passes": true
  },
  {
    "id": "AC-004",
    "category": "security",
    "description": "NEPV 接口和页面严格执行决策者 Session 与只读边界。",
    "steps": [
      "分别使用未登录、NEPS、NEPG、NEPM 和有效 NEPV Session 访问新增接口与 /nepv/portal。",
      "提交非法日期范围、非法省市组合，并尝试调用不存在的写操作。",
      "验证未登录返回 401、角色不符返回 403、非法参数返回 400；有效 NEPV 只能读取数据，响应不含密码、Session 或写操作入口。"
    ],
    "passes": true
  },
  {
    "id": "AC-UI-UX",
    "category": "integration",
    "description": "Chrome MCP 验证 NEPV 中国地图决策大屏在桌面与窄屏下的交互、可读性和控制台状态。",
    "steps": [
      "在 Chrome MCP 以 1440x900 打开 /nepv/portal，验证全国地图、省内市级地图、筛选、统计卡片、AQI 分布、趋势、覆盖率、城市数据表和预警列表的布局、真实数据与空状态。",
      "切换到 375x812，验证页面可纵向阅读，筛选、地图、预警列表和重置操作无裁切、重叠或不可达；这不是新增移动端业务功能。",
      "悬停地图省份、市级区域、筛选控件、数据行和重置按钮，验证视觉反馈和说明明确；点击省份验证市级下钻、统计联动及返回全国。",
      "审计完整流程的浏览器控制台，确认 JavaScript 错误为零。",
      "存档 TASK-010-nepv-decision-desktop.png 与 TASK-010-nepv-decision-mobile.png。"
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
| db | false | 复用既有检测、反馈、预警、地区和网格员表，不改变结构。 |
| ui | true | `docs/2-designs/ui_prototype.md` |
| constraints | false | 既有 Session、真实数据和只读边界足以覆盖。 |
| adr | false | 不引入新运行时依赖、持久化方案、外部服务或不可逆决策。 |
| agent-runtime | false | 端口、配置、启动命令、测试和 Lint 命令不变。 |

### Approval-sensitive changes

- None。已确认采用项目内 Vue + ECharts + DataV GeoAtlas 生成的静态 GeoJSON；不接入外部 DataV 页面、天池服务或地图密钥。

### Explicit non-maintenance

- `docs/2-designs/db_schema.md` 不修改：区域编码映射作为前端静态地图资源维护，不改变现有内部地区主键。
- `docs/3-constraints/`、ADR、根目录及子模块 `AGENTS.md`、`.codex` Hook 和初始化脚本不修改：本任务不形成新的长期工程约束或运行时方式。
