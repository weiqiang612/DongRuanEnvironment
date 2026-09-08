# TASK-003: 前后端代码基线重构

**Status**: Draft
**Created**: 2026-09-08
**Feature dir**: `docs/4-tasks/features/TASK-003-baseline-code-refactor/`

## Objective

在不新增业务能力或改变正式接口路径的前提下，清理脚手架和风险代码，使现有反馈维护切片的前后端实现符合项目约束、数据库映射和政务业务系统 UI 基线。

## Scope

### In scope

- 清理后端测试控制器、代码生成器及其不再使用的依赖、硬编码连接信息、调试输出和废弃注释。
- 将控制器调整为 HTTP/校验职责，使用请求 DTO、构造器注入和服务层承载省市查询；不让控制器直接访问 `JdbcTemplate`。
- 将 `AqiFeedback` 与已执行的数据库结构对齐，包含字符串网格员编号、规范时间、状态和超时字段。
- 保持既有反馈和地区接口的路径、方法及既有 JSON 字段兼容；同步补充其类型、校验和错误响应契约。
- 清理 Vue 默认欢迎页、About 页面、示例组件/图标和计数器 Store；将 `/` 重定向到 `/aqiFeedback`，保留现有反馈维护能力。
- 将前端 API 类型、视图职责和错误反馈对齐现有 Vue/Pinia 约束。
- 修复工程 UTF-8 编译编码，并完成后端、前端和浏览器验证。

### Out of scope

- 不新增认证、任务指派、AQI 检测、预警、超时处置、统计或决策大屏业务。
- 不新增或升级依赖、插件、UI 组件库、全局状态机制、数据库迁移或数据清理。
- 不改变正式反馈和地区接口的路径、HTTP 方法、`ResultVO` 外层结构或数据库连接配置。
- 不改动 `DemoApplication` 类名、后端目录结构或 Java 根包名。

## Acceptance criteria

```json
[
  {
    "id": "AC-001",
    "category": "functional",
    "description": "现有反馈和地区接口保留原有路径、HTTP 方法和既有 JSON 字段，并在输入无效时返回一致、可理解的校验错误。",
    "steps": ["核对反馈和地区接口的方法、路径及 ResultVO 外层结构。", "使用缺失或非法的反馈请求字段调用接口。", "验证请求被安全拒绝，且不泄露内部实现。"],
    "passes": false
  },
  {
    "id": "AC-002",
    "category": "integration",
    "description": "反馈实体、DTO、服务和数据访问实现与 db_schema.md 的现有字段类型和逻辑外键策略一致，且不执行新的数据库迁移。",
    "steps": ["核对 gmId、规范时间、状态和超时字段的 Java 类型与数据库设计。", "核对省市查询已离开控制器的数据访问职责。", "验证迁移目录和实际数据库结构未因本任务改变。"],
    "passes": false
  },
  {
    "id": "AC-003",
    "category": "security",
    "description": "生产源码不再包含测试接口、代码生成器、硬编码数据库连接信息、调试输出或废弃注释。",
    "steps": ["搜索测试控制器、代码生成器和连接凭据模式。", "检查 Maven 依赖与生产源码。", "验证不再存在对应生产代码或凭据。"],
    "passes": false
  },
  {
    "id": "AC-004",
    "category": "edge-case",
    "description": "首页和反馈维护页不依赖 Vite 默认脚手架，且前端请求错误能够被页面理解性地反馈。",
    "steps": ["访问 / 并验证跳转到 /aqiFeedback。", "确认默认欢迎页、About 页、示例组件和计数器 Store 无路由或导入引用。", "模拟或检查失败请求的错误反馈路径。"],
    "passes": false
  },
  {
    "id": "AC-UI-UX",
    "category": "integration",
    "description": "Chrome 验证反馈维护页在桌面和移动端保持经典政务业务系统风格，并且交互和控制台状态干净。",
    "steps": ["在 Chrome MCP 以 1440x900 打开 /aqiFeedback，验证布局、间距、可见性和溢出正常，并存档截图 TASK-003-aqi-feedback-desktop.png。", "切换到 375x812，验证页面、主要操作和表单没有裁切或重叠，并存档截图 TASK-003-aqi-feedback-mobile.png。", "悬停页面上的主要按钮、表格操作和导航元素，验证具有预期的视觉反馈。", "在流程期间审计浏览器控制台，验证零 JavaScript 错误。", "验证根路径跳转至 /aqiFeedback，且不存在 Vite 默认欢迎页。"],
    "passes": false
  }
]
```

## Notes

### Documentation impact

| Area | Impacted | Maintenance target |
|---|---:|---|
| requirements | false | 业务范围不变。 |
| architecture | false | 现有分层设计不变，本任务使实现与其一致。 |
| api | true | `docs/2-designs/api_contract.md` |
| db | true | `docs/2-designs/db_schema.md` 中的实体映射说明。 |
| ui | true | `docs/2-designs/ui_prototype.md` 中的当前页面与入口说明。 |
| constraints | false | 复用既有约束，不新增长期规则。 |
| adr | false | 未引入新依赖或架构策略。 |
| agent-runtime | false | 启动方式、端口和模块结构不变。 |

### Approval-sensitive changes

- 删除测试控制器、代码生成器、无用前端脚手架和仅供代码生成器使用的依赖；用户已于 2026-09-08 明确授权。
- 不新增环境变量、配置键、数据库迁移或数据操作。

### Explicit non-maintenance

- `docs/1-requirements/`、`docs/2-designs/architecture.md`、`docs/3-constraints/` 和运行脚本不维护，因为业务范围、架构边界、约束和运行方式均不改变。
