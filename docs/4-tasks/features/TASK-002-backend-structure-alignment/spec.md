# TASK-002: 后端目录与包名迁移收尾

**Status**: In Progress
**Created**: 2026-09-08
**Feature dir**: `docs/4-tasks/features/TASK-002-backend-structure-alignment/`

## Objective

将已完成的后端目录提升与 Java 包名迁移同步到项目脚本、工程指引和设计记录，使后端可从 `back/` 作为唯一 Maven 根目录稳定构建和启动。

## Scope

### In scope

- 将仓库中有效的旧后端二级目录引用调整为 `back`。
- 将有效的旧示例 Java 根包引用调整为 `com.dongruan.environment`。
- 同步启动脚本、模块指引、架构/数据库设计路径和构建约束。
- 使用新的后端根目录运行回归测试，并检查旧路径和包名不再残留。

### Out of scope

- 不改变现有接口路径、方法、请求/响应结构或认证行为。
- 不修改数据库连接、表结构、迁移内容或现有数据。
- 不删除脚手架页面/控制器/生成器、不调整 Maven 依赖或坐标、不重命名 `DemoApplication`。
- 不修改前端代码、路由或 UI。

## Acceptance criteria

```json
[
  {
    "id": "AC-001",
    "category": "functional",
    "description": "后端以 back 作为 Maven 根目录，可使用新的包装器命令完成构建测试。",
    "steps": ["确认 back/pom.xml、back/mvnw 和 back/src 存在。", "在 back 目录运行 ./mvnw test。", "验证测试全部通过。"],
    "passes": true
  },
  {
    "id": "AC-002",
    "category": "integration",
    "description": "项目启动脚本、模块指引和设计文档均引用新的后端目录与 Java 包名。",
    "steps": ["检查 init.ps1、init.sh、根 AGENTS.md 与 back/AGENTS.md。", "检查 architecture.md、db_schema.md 和构建约束中的后端路径。", "验证路径指向 back，入口包为 com.dongruan.environment。"],
    "passes": true
  },
  {
    "id": "AC-003",
    "category": "edge-case",
    "description": "仓库有效源文件、脚本和文档不再残留旧目录或旧 Java 包引用。",
    "steps": ["搜索旧后端二级目录与旧示例 Java 根包。", "排除 Git 历史和构建产物后检查命中。", "验证不存在有效引用。"],
    "passes": true
  },
  {
    "id": "AC-004",
    "category": "security",
    "description": "目录和包名迁移不复制数据库凭据，也不执行数据库迁移或数据操作。",
    "steps": ["检查本任务新增或修改文件不包含数据库密码。", "验证数据库迁移脚本内容未改变。", "验证未新增环境变量或配置键。"],
    "passes": true
  }
]
```

## Notes

### Documentation impact

| Area | Impacted | Maintenance target |
|---|---:|---|
| requirements | false | 业务范围不变。 |
| architecture | true | `docs/2-designs/architecture.md` |
| api | false | 接口契约不变。 |
| db | true | `docs/2-designs/db_schema.md` 中的迁移与配置路径。 |
| ui | false | 前端页面与交互不变。 |
| constraints | true | `docs/3-constraints/always-do.md` 中的后端验证命令。 |
| adr | false | 未引入新的架构策略。 |
| agent-runtime | true | 根 `AGENTS.md`、`back/AGENTS.md`、`init.ps1`、`init.sh`。 |

### Approval-sensitive changes

- 核心目录结构已由用户于 2026-09-08 手动完成；本任务仅同步引用和验证。
- 不新增环境变量、配置键、依赖或数据库迁移。

### Explicit non-maintenance

- `docs/1-requirements/`、`docs/2-designs/api_contract.md`、`docs/2-designs/ui_prototype.md` 不维护，因为业务、接口和 UI 均不改变。
