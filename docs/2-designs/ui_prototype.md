# 原型与 UI 设计

## 当前页面

- `HomeView.vue`：应用首页。
- `AqiFeedBackList.vue`：反馈列表、增删改及省市选择。
- `AboutView.vue`：示例说明页。

## 规则

将可复用请求逻辑放入 `src/api/`，页面状态留在视图或 Pinia store。新增路由时同步更新 `src/router/index.ts`；表单修改必须与 `api_contract.md` 的请求字段保持一致。
