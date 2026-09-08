# Never Do

## Security

- 🚫 不得提交、复制或记录真实密钥、令牌、数据库凭据或个人数据。
- 🚫 不得将 `application.yaml` 中的数据库凭据扩散到新文件、日志或文档。

## Contracts

- 🚫 不得在未更新 `docs/2-designs/api_contract.md` 的情况下改变接口方法、路径、字段或响应结构。
- 🚫 不得在未更新 `docs/2-designs/db_schema.md` 的情况下改变实体、SQL、索引或表结构。
- 🚫 不得以删除、跳过或弱化测试和校验的方式使构建通过。

## Boundaries

- 🚫 不得直接编辑 `node_modules/`、`target/`、`dist/` 或 IDE 生成文件。
- 🚫 不得在没有活动任务范围的情况下进行无关重构。
