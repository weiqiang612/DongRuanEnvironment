# TASK-010: Tasks

**Spec**: `spec.md`
**Status**: Completed

## Key decisions

- NEPV 决策大屏在项目内的 Vue 页面实现，复用已有 ECharts；中国省级与省内市级 GeoJSON 由 DataV GeoAtlas 准备为本地静态资源，不嵌入或调用外部 DataV/天池运行时服务。
- 地图展示全国省级风险着色，点击省份后下钻至市级 AQI 风险分布；区县下钻、地图调度、实时定位和轨迹不在本任务范围。
- 网格覆盖率固定按“至少配置一名网格员的城市数 ÷ 全部城市数”实时计算；区域数据、预警和统计均只读聚合，不新增数据库结构或汇总表。
- 新增接口只接受 `NEPV_DECISION_MAKER` HTTP Session；NEPV 不拥有预警处置、任务调度或检测结果写权限。

## Progress

- [x] T1 — 更新 `docs/1-requirements/project_overview.md` 与 `docs/1-requirements/requirements_analysis.md`，固化 TASK-010 的项目内中国地图、只读决策大屏、覆盖率公式与非目标 · covers: doc-maintenance, AC-001, AC-002, AC-003, AC-004
- [x] T2 — 更新 `docs/2-designs/architecture.md`，定义 NEPV Session 校验、地图区域编码适配和只读统计聚合链路 · covers: doc-maintenance, AC-001, AC-002, AC-003, AC-004
- [x] T3 — 更新 `docs/2-designs/api_contract.md`，定义 NEPV 决策统计接口、筛选字段、地图省级指标、覆盖率、预警摘要和 401/403/400 边界 · covers: AC-001, AC-002, AC-003, AC-004
- [x] T4 — 更新 `docs/2-designs/ui_prototype.md`，定义深蓝中国地图驾驶舱的布局、省份悬停/点击联动、空状态和只读预警展示 · covers: doc-maintenance, AC-001, AC-002, AC-UI-UX
- [x] T5 — 新增 NEPV 决策统计响应 DTO、前端 TypeScript 类型及内部省份名称/ID 到国家 adcode 的静态映射约定，明确无法匹配时的安全降级 · covers: AC-001, AC-002, AC-003, AC-004
- [x] T6 — 扩展 Mapper 查询，读取筛选范围内的检测结果、AQI 预警、地区城市与网格员覆盖事实，避免读取或写入无关业务数据 · covers: AC-001, AC-003, AC-004
- [x] T7 — 实现 NEPV 决策统计服务和 Controller：实时聚合省级地图指标、AQI 分布、月度趋势、覆盖率和最新预警，并校验决策者 Session 与参数 · covers: AC-001, AC-002, AC-003, AC-004
- [x] T8 — 补充后端测试，覆盖真实聚合、省级与城市风险数据、覆盖率分母为零、预警摘要、未登录/越权/非法日期和只读边界 · covers: AC-001, AC-002, AC-003, AC-004
- [x] T9 — 将 DataV GeoAtlas 生成的中国省级与省内市级 GeoJSON 及其来源说明纳入前端地图资源，按省份按需加载并核对项目省份 adcode 映射 · covers: AC-001, AC-002, AC-UI-UX
- [x] T10 — 新增 `front/src/api/nepv.ts` 的决策统计请求函数与类型，保持 `ResultVO`、省市日期筛选和错误处理契约 · covers: AC-001, AC-003, AC-004
- [x] T11 — 将 `/nepv/portal` 升级为深蓝中国地图决策驾驶舱，实现筛选、地图悬停、全国省级地图到省内市级地图下钻、指标/趋势/覆盖率/城市数据表/预警展示及空状态，不新增写操作；主画布为 1920×1080 三栏布局 · covers: AC-001, AC-002, AC-003, AC-UI-UX
- [x] T12 — 用户手工 Chrome 验收 `/nepv/portal`：完成桌面与 375×812 窄屏检查、地图下钻/城市筛选/返回全国/重置与 AQI 配色检查；修复窄屏 ECharts `resize()` 竞争后，用户确认控制台无项目 JavaScript 错误。内置浏览器连接异常未作为验收工具使用 · covers: AC-UI-UX
- [x] T13 — 运行 `cd back && ./mvnw test`，72 项测试全部通过
- [x] T14 — 运行 `cd front && npm run build` 与 `cd front && npm run lint`，构建通过且无 Lint 违规
- [x] T15 — 逐项验证 `spec.md` 的验收标准，并将通过项的 `passes` 更新为 `true`
- [x] T16 — 更新 `docs/4-tasks/DEVELOPMENT_ROADMAP.md` 与 `docs/4-tasks/CURRENT_PLAN.md`，记录 TASK-010 完成状态、实际验证结果与下一阶段入口

## Dependencies

- T5 至 T7 依赖 T1 至 T4 的业务、架构与接口契约；T9 至 T11 依赖 T3、T4 及后端接口。
- T7 的所有统计必须从现有表实时聚合；T9 的 GeoJSON 与 adcode 映射必须在不匹配时安全降级，禁止伪造地图数据。
- T8、T12 至 T16 依赖全部实现任务完成。

## Acceptance evidence

- 后端完整测试：72 项通过；包含 NEPV Session 401/403、非法日期 400、实时聚合、城市风险下钻、覆盖率分母为零和只读边界。
- 前端：生产构建、类型检查和 Lint 通过；市级 GeoJSON 为点击省份后按需加载。
- 用户手工 Chrome 验收：桌面及 375×812 窄屏下，确认地图下钻、城市筛选、统计联动、AQI 图表/标签配色、下钻动效和页面可读性；修复 ECharts 窄屏 resize 竞争后，确认无项目 JavaScript 错误。
