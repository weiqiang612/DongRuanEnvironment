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

## From dev-standards repo

来源：`weiqiang612/dev-standards` 的 `universal/zh/never-do.md`、`java/zh/java-core.md`、`java/zh/spring-conventions.md`、`vue/zh/vue-core.md`、`vue/zh/state-management.md`。

### 通用

- 🚫 不得以宽泛忽略指令绕过 Linter、静态分析或编译器警告；确有例外时必须附带任务链接说明。
- 🚫 不得对受保护分支执行强推，不得使用 `--no-verify` 绕过本地校验。
- 🚫 不得提交超过 5MB 的大型二进制资产；应使用 Git LFS 或外部对象存储。
- 🚫 不得在生产源文件保留被注释掉的代码、临时调试输出或环境特定端点；配置应外置，调试代码应删除。
- 🚫 不得使用循环手动复制数组或对象；前端应使用展开运算符或 `Object.assign()`。
- 🚫 不得将可变数组或对象作为函数默认参数。

### Java 与 Spring Boot

- 🚫 不得使用 `var`、`new Thread(...)` 或 `Executors` 工厂方法创建线程池；线程池使用 `ThreadPoolExecutor` 并明确容量边界。
- 🚫 不得从 Service 层向外声明或抛出受检异常，不得在 `finally` 中返回。
- 🚫 不得共享未保护的 `SimpleDateFormat`，不得使用 `==` 比较包装类型，不得使用魔法值或内联业务字符串。
- 🚫 不得直接强转或在迭代 `subList()` 时结构性修改原集合；需要增删元素时不得使用 `Arrays.asList()` 生成的固定大小列表。
- 🚫 不得使用拼音、中文、下划线或美元符号作为类、方法、变量命名；POJO 字段不得设置默认值，布尔字段不得以 `is` 开头。
- 🚫 不得产生 Spring Bean 循环依赖，也不得用 `@Order` 或延迟初始化掩盖它。
- 🚫 不得跳过公开 Controller 或 RPC 入口的参数校验，不得使用字段注入。
- 🚫 不得在 Controller、DAO 或 Repository 层使用 `@Transactional`，也不得在活动数据库事务中执行慢速外部网络调用。

### Vue 3 与 Pinia

- 🚫 新增或重构 Vue 组件不得使用 Options API、`reactive()` 或直接 DOM 查询；使用 Composition API、`<script setup>` 和模板 ref。
- 🚫 不得直接修改子组件 `props`，不得在 `computed()` 中修改状态或产生副作用。
- 🚫 模板中不得包含业务计算、API 调用或复杂 JavaScript 表达式；复杂逻辑应抽取到 `computed` 或脚本层。
- 🚫 不得使用单词名称的单文件组件，不得在同一元素上同时使用 `v-if` 与 `v-for`。
- 🚫 不得新增 Vuex 或 Pinia Options Store；全局状态统一使用 Pinia Setup Store。
- 🚫 不得直接赋值修改 Store 状态，不得直接解构 Store 实例；状态变更通过 action，响应式属性通过 `storeToRefs()` 获取。
