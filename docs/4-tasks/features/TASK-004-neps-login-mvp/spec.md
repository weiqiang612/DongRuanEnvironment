# TASK-004: NEPS 公众监督员登录 MVP

**Status**: Draft
**Created**: 2026-09-08
**Feature dir**: `docs/4-tasks/features/TASK-004-neps-login-mvp/`

## Objective

为公众监督员提供可在 PC 端使用的统一入口与真实登录能力，以安全校验现有 `supervisor` 账号并进入当前反馈维护页面。

## Scope

### In scope

- 新增 PC 端统一入口 `/`，展示 NEPS、NEPG、NEPM、NEPV 四端入口；仅 NEPS 可进入登录页，其他端明确提示暂未开放。
- 新增 PC 端 NEPS 登录页 `/neps/login`，复用 `front/src/assets/` 中的品牌、Logo、山水插画与山水背景素材。
- 新增 `POST /auth/neps/login`，使用手机号和密码认证现有 `supervisor` 账号；成功后创建 HTTP Session，并由前端跳转至 `/aqiFeedback`。
- 通过数据库迁移为 `supervisor` 增加密码哈希存储；使用 JDK PBKDF2 校验密码，不保存、不返回或记录明文密码。
- 更新需求、架构、接口、数据库与 UI 文档，并补充后端和前端验证。

### Out of scope

- 不实现注册、找回密码、欢迎页、退出登录、JWT、角色权限拦截或反馈归属隔离。
- 不实现 NEPG、NEPM、NEPV 的登录、业务页面或后端认证。
- 不制作移动端页面或移动端浏览器验收；本任务仅验收 PC 桌面布局。
- 不新增或升级依赖、外部认证服务、环境变量或运行时配置。

## Acceptance criteria

```json
[
  {
    "id": "AC-001",
    "category": "functional",
    "description": "公众监督员可从统一入口进入 NEPS 登录页，使用有效手机号和密码登录，并跳转至 /aqiFeedback。",
    "steps": ["访问 / 并选择 NEPS 公众监督员端。", "在 /neps/login 输入已配置密码哈希的 supervisor 手机号和正确密码。", "验证接口返回成功、服务端建立会话，前端跳转至 /aqiFeedback。"],
    "passes": false
  },
  {
    "id": "AC-002",
    "category": "edge-case",
    "description": "登录请求缺少字段、手机号不存在或密码错误时被安全拒绝，页面提供可理解的错误反馈。",
    "steps": ["分别提交空手机号、空密码、未知手机号和错误密码。", "验证接口返回一致的 400 或 401 ResultVO 错误，前端停留在登录页并显示失败提示。", "验证错误消息不泄露账号是否存在或密码校验细节。"],
    "passes": false
  },
  {
    "id": "AC-003",
    "category": "security",
    "description": "密码仅以 PBKDF2 哈希形式存储和比对，登录响应、日志、测试和前端状态均不包含明文密码或密码哈希。",
    "steps": ["检查 supervisor 迁移、认证服务、接口 DTO 与响应对象。", "验证密码字段未被返回、打印或持久化为明文。", "验证认证查询与密码比较在服务层完成。"],
    "passes": false
  },
  {
    "id": "AC-004",
    "category": "integration",
    "description": "统一入口只开放 NEPS 登录入口，其余三端不进入未实现的路由；登录接口与数据库映射符合已更新的接口和数据库契约。",
    "steps": ["点击 NEPS 卡片并验证进入 /neps/login。", "点击其余三端卡片并验证显示暂未开放提示且不改变路由。", "核对登录请求、响应、supervisor 密码哈希字段与迁移文档。"],
    "passes": false
  },
  {
    "id": "AC-UI-UX",
    "category": "integration",
    "description": "Chrome 在 PC 桌面尺寸验证统一入口和 NEPS 登录页复用提供素材，并保持清晰、可操作的政务业务系统风格。",
    "steps": ["在 Chrome 以 1440x900 打开 /，验证四端入口、品牌素材、布局、间距与溢出正常，并存档 TASK-004-entry-desktop.png。", "在同一尺寸打开 /neps/login，验证品牌图、山水背景、登录表单、错误提示和登录按钮可见可用，并存档 TASK-004-neps-login-desktop.png。", "悬停 NEPS 卡片、暂未开放卡片、登录按钮及注册提示，验证预期视觉反馈。", "审计浏览器控制台，验证零 JavaScript 错误。", "本任务按用户明确范围仅验收 PC 端，不执行移动端页面或 375x812 验收。"],
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
| db | true | `docs/2-designs/db_schema.md` |
| ui | true | `docs/2-designs/ui_prototype.md` |
| constraints | false | 现有安全、依赖与数据库审批规则足以覆盖。 |
| adr | false | 使用 JDK 标准 PBKDF2 与最小 HTTP Session，不引入长期架构策略或新依赖。 |
| agent-runtime | false | 端口、启动命令、代理和配置键不变。 |

### Approval-sensitive changes

- 新增数据库迁移，为 `supervisor` 增加密码哈希字段；用户于 2026-09-08 在确认实施方案时已授权。
- 不新增依赖、环境变量、配置键或外部认证服务。

### Explicit non-maintenance

- `docs/3-constraints/`、ADR、根目录和模块 `AGENTS.md`、启动脚本不维护，因为长期约束、依赖策略、端口和运行方式不改变。
