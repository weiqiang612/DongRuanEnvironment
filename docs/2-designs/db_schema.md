# 数据库设计

## 连接基线

后端配置连接 MySQL 8 的 `nepmdb`。数据库凭据目前写在 `back/demo/src/main/resources/application.yaml`，不得向日志、提交信息或新文件复制该凭据；迁移至本地环境变量或受控配置前先确认。

## 已扫描实体与表

| 表 | 实体/用途 | 关键字段 |
|---|---|---|
| `aqi_feedback` | `AqiFeedback`，公众监督反馈 | `af_id`、`tel_id`、`province_id`、`city_id`、`state` |
| `grid_province` | 省级区域选项 | `province_id`、`province_name` |
| `grid_city` | 按省筛选的市级选项 | `city_id`、`province_id`、`city_name` |

## 规则

实体字段、SQL 列和 API JSON 字段的对应关系必须一致。新增或变更表、字段、索引、约束或状态语义前，更新本文件并先确认迁移与回滚方案。
