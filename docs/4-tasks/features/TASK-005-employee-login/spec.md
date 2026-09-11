# TASK-005: NEPG、NEPM 与 NEPV 员工真实登录

**Status**: Completed
**Created**: 2026-09-09
**Feature dir**: `docs/4-tasks/features/TASK-005-employee-login/`

## Objective

为公众监督员、AQI 检测网格员、系统管理员和决策者提供统一的 PC 端登录视图；保持各端既有身份来源与认证边界，并以明确角色区分 NEPM 的管理权限与 NEPV 的只读决策身份。

## Scope

### In scope

- 四端登录路由 `/neps/login`、`/nepg/login`、`/nepm/login`、`/nepv/login` 复用同一 PC 登录视图；NEPS 保持手机号和密码登录、既有 `/auth/neps/login` 接口与 `/aqiFeedback` 成功跳转不变。
- NEPG 使用 `grid_member.gm_code` 作为账号标识；NEPM 和 NEPV 共用 `admins` 身份来源，并使用 `admins.admin_code` 登录。
- 为 `grid_member` 与 `admins` 的登录密码设计 PBKDF2 存储兼容迁移；为 `admins` 增加显式角色值 `NEPM_ADMIN` 或 `NEPV_DECISION_MAKER`，角色为空或不匹配时不得登录对应端。
- 新增三个员工端登录接口，复用 JDK PBKDF2、`ResultVO` 和最小 HTTP Session；成功响应只返回非敏感身份信息与角色。
- 登录成功分别进入 `/nepg/portal`、`/nepm/portal`、`/nepv/portal` 的最小落地页，只确认登录态和端别；不接入尚未实现的业务功能。
- 更新需求、架构、接口、数据库、UI 文档与 ADR，并补充后端、前端和 PC 浏览器验收。

### Out of scope

- 不创建、猜测、导出或记录任何默认账号、密码或密码哈希；实际成功登录仅使用用户后续配置的真实账号。
- 不实现注册、找回密码、退出登录、JWT、全局拦截器、反馈归属隔离、HR 外部接口或三端后续业务模块。
- 不新增独立决策者账号表；NEPV 仅复用 `admins` 并以角色区分。
- 不制作移动端页面或移动端浏览器验收；本任务仅验收 PC 桌面布局。
- 不新增依赖、环境变量或运行时配置。

## Acceptance criteria

```json
[
  {
    "id": "AC-001",
    "category": "functional",
    "description": "用户可从统一入口进入四端统一登录视图；NEPS 保持既有成功跳转，配置了正确身份、密码和角色的员工账号进入对应端的最小落地页。",
    "steps": ["在 / 依次选择四端卡片。", "验证 NEPS 仍使用手机号登录并进入 /aqiFeedback；分别提交有效的网格员账号，以及具备 NEPM_ADMIN 或 NEPV_DECISION_MAKER 角色的管理员账号。", "验证接口返回成功、Session 仅写入非敏感身份和角色，且员工端进入匹配的 /nepg/portal、/nepm/portal 或 /nepv/portal。"],
    "passes": true
  },
  {
    "id": "AC-002",
    "category": "edge-case",
    "description": "缺少字段、未知账号、错误密码、角色为空或角色与入口不匹配时，接口安全拒绝请求且页面保留在当前登录页。",
    "steps": ["分别提交空账号、空密码、未知账号、错误密码和角色不匹配的管理员账号。", "验证字段错误返回 HTTP 400，认证或角色失败统一返回 HTTP 401。", "验证失败信息不泄露账号存在性、密码校验或角色数据。"],
    "passes": true
  },
  {
    "id": "AC-003",
    "category": "security",
    "description": "网格员与管理员密码均以 PBKDF2 形式保存和比较，且 NEPV 身份只能通过 admins 表的显式决策者角色取得。",
    "steps": ["核对 grid_member 与 admins 的迁移、回滚脚本和服务层认证逻辑。", "验证登录响应、Session、日志、测试和前端状态不包含明文密码或密码哈希。", "验证迁移不插入默认账号或默认密码，角色为空的管理员不能登录。"],
    "passes": true
  },
  {
    "id": "AC-004",
    "category": "integration",
    "description": "三端登录接口、数据库映射、管理员角色及前端 API 客户端与更新后的契约一致。",
    "steps": ["核对 POST /auth/nepg/login、POST /auth/nepm/login、POST /auth/nepv/login 的请求、成功和失败 ResultVO 契约。", "在执行迁移前核对真实表字段、受影响行数和备份要求，再验证迁移与回滚脚本符合 db_schema.md。", "验证前端请求路径、路由和成功跳转与 api_contract.md 一致。"],
    "passes": true
  },
  {
    "id": "AC-UI-UX",
    "category": "integration",
    "description": "Chrome 在 PC 桌面尺寸验证三端入口、登录页与最小落地页均清晰、可操作，且控制台无 JavaScript 错误。",
    "steps": ["在 1440x900 访问 /、/nepg/login、/nepm/login、/nepv/login，验证布局、品牌、表单、错误提示与溢出正常，并存档 TASK-005-entry-desktop.png、TASK-005-nepg-login-desktop.png、TASK-005-nepm-login-desktop.png、TASK-005-nepv-login-desktop.png。", "在 1366x768 验证三张登录页和三个最小落地页的可见性及可操作性。", "悬停三张入口卡片、登录按钮和返回入口操作，验证预期视觉反馈。", "审计浏览器控制台，验证零 JavaScript 错误。", "本任务按既定范围仅验收 PC 端，不执行 375x812 移动端验收。"],
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
| db | true | `docs/2-designs/db_schema.md` |
| ui | true | `docs/2-designs/ui_prototype.md` |
| constraints | false | 既有密码、凭据、迁移与审批边界足以覆盖。 |
| adr | true | `docs/3-constraints/adr/ADR-001-shared-admin-identity.md` |
| agent-runtime | false | 端口、启动命令、代理和配置键不变。 |

### Key decision

- 用户于 2026-09-09 选择：NEPV 不新增独立账号表，复用 `admins` 身份来源，并通过 `NEPV_DECISION_MAKER` 角色与 `NEPM_ADMIN` 区分；三端员工均以业务编号与密码登录，NEPG 使用 `gm_code`，NEPM、NEPV 使用 `admin_code`。
- 2026-09-11：用户确认 TASK-005 已完成人工验收，按验收结果将本任务标记为完成。

### Approval-sensitive changes

- 新增或修改 `grid_member`、`admins` 的登录密码和角色迁移，执行前必须备份受影响表并核对真实字段、行数与现存凭据格式。
- 不新增依赖、环境变量、运行时配置或默认账号。

### Explicit non-maintenance

- 根目录和模块 `AGENTS.md`、启动脚本及 `.codex/` 不维护，因为运行方式和质量门禁不改变。
