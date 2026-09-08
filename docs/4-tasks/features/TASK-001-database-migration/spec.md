# TASK-001: 数据库迁移与反馈检测闭环

**Status**: In Progress
**Created**: 2026-09-08
**Feature dir**: `docs/4-tasks/features/TASK-001-database-migration/`

## Objective

在不重复创建现有业务表的前提下，迁移本地 `nepmdb`，使反馈状态、超时、检测结果、指派留痕和高等级 AQI 预警具备可追溯的数据基础。

## Scope

### In scope

- 扩展 `aqi_feedback` 的规范时间、超时字段、状态索引和网格员标识类型，并为旧接口未传 `submitted_at` 的情况提供当前时间默认值。
- 将空的 `statistics` 重构为 `detection_result`，并以反馈唯一关联检测结果。
- 创建 `task_assign_log` 和仅用于高等级 AQI 的 `alert_record`。
- 提供版本化正向迁移与受限回滚脚本，执行迁移并核验结构。
- 移除全部物理外键，改用服务层校验的逻辑外键，并修复迁移造成的中文注释乱码。

### Out of scope

- 不执行历史初始化 SQL 或 `省市.sql`，不创建 `province`、`city`、`public_user`、`grid_region`、`inspection_task`。
- 不新增超时独立预警、认证、HR 集成、API、页面或业务处理代码。
- 不修改或暴露本地数据库凭据。

## Acceptance criteria

```json
[
  {
    "id": "AC-001",
    "category": "functional",
    "description": "反馈表具备可计算的提交、指派、完成和超时数据，且保留现有接口字段。",
    "steps": ["检查 aqi_feedback 含规范时间和超时字段。", "验证历史 submitted_at 已从旧日期时间回填。", "验证状态仅使用 0、1、2。"],
    "passes": true
  },
  {
    "id": "AC-002",
    "category": "integration",
    "description": "检测结果、指派日志和高等级预警结构与数据库设计契约一致，全部关联采用逻辑外键。",
    "steps": ["验证 statistics 已更名为 detection_result 且 feedback_id 唯一。", "验证 task_assign_log 与 alert_record 的关联字段和索引。", "验证 alert_record 不包含超时预警类型。", "验证 information_schema 中物理外键数量为 0。"],
    "passes": true
  },
  {
    "id": "AC-003",
    "category": "edge-case",
    "description": "迁移前置条件阻止在旧统计记录、非法状态或不可解析时间存在时执行结构重构。",
    "steps": ["执行迁移前检查。", "验证 statistics 行数为 0、状态合法、历史时间可解析。", "任一前置条件失败时不执行 DDL。"],
    "passes": true
  },
  {
    "id": "AC-004",
    "category": "security",
    "description": "迁移过程不将数据库凭据写入仓库、脚本或命令输出。",
    "steps": ["从本地 application.yaml 读取连接配置。", "检查新增文件不包含连接密码。", "验证命令输出不打印凭据。"],
    "passes": true
  }
]
```

## Notes

### Documentation impact

| Area | Impacted | Maintenance target |
|---|---:|---|
| requirements | false | 业务范围不变。 |
| architecture | false | 分层与集成边界不变。 |
| api | false | 尚未实现新接口。 |
| db | true | `docs/2-designs/db_schema.md` |
| ui | false | 页面尚未实施。 |
| constraints | false | 现有迁移约束适用。 |
| adr | false | 未引入新的架构决策。 |
| agent-runtime | false | 不修改。 |

### Approval-sensitive changes

- 本地 MySQL 表、字段、索引、约束和数据回填迁移；用户已于 2026-09-08 明确授权。
- 不新增环境变量或配置键；凭据仅从本地 `application.yaml` 读取。

### Explicit non-maintenance

- `docs/1-requirements/`、`docs/2-designs/api_contract.md`、`docs/2-designs/architecture.md` 和 `docs/2-designs/ui_prototype.md` 描述的业务边界不因本次结构迁移改变。
