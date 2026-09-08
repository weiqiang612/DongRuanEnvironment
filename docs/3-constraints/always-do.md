# Always Do

## Session and task work

- ✅ 每次会话先阅读 `docs/4-tasks/CURRENT_PLAN.md` 和活动任务的 `spec.md`。
- ✅ 非简单改动前检查 `never-do.md`；环境未启动时仅报告状态，等待明确的人为启动决定。
- ✅ 变更 API 或数据库前阅读并同步 `docs/2-designs/api_contract.md` 与 `docs/2-designs/db_schema.md`。

## Quality

- ✅ 后端改动完成前运行 `cd back && ./mvnw test`。
- ✅ 前端改动完成前运行 `cd front && npm run build` 与 `npm run lint`。
- ✅ 前端遵守两空格、单引号、无分号、100 字符行宽的现有格式化规则。
- ✅ 完成后更新任务 `tasks.md` 与 `CURRENT_PLAN.md`，并记录实际验证结果。

## From dev-standards repo

来源：`weiqiang612/dev-standards` 的 `universal/zh/always-do.md`、`java/zh/java-core.md`、`java/zh/spring-conventions.md`、`vue/zh/vue-core.md`、`vue/zh/state-management.md`。

### 通用

- ✅ 提交或推送前运行对应测试、格式化与 Linter，并检查暂存区 `git diff`，确认不含凭据、调试日志或临时变通代码。
- ✅ 新建功能分支前同步主分支最新状态；提交信息使用清晰的约定式格式；Pull Request 说明变更、原因和测试结果。
- ✅ 保持方法、组件和类单一职责；复杂度或体积升高时及时拆分。
- ✅ 获取对象属性时优先使用解构赋值。

### Java 与 Spring Boot

- ✅ 方法参数使用 `final`；局部变量在可行时使用 `final`；POJO 与 DTO 重写 `toString()`。
- ✅ POJO 属性和 RPC 参数使用包装类型；对象比较使用 `Objects.equals()` 或常量在前的 `.equals()`；重写 `equals()` 时同步重写 `hashCode()`。
- ✅ 创建集合时指定合理初始容量，遍历 Map 使用 `entrySet()` 或 `Map.forEach()`；正则使用预编译 `static final Pattern`。
- ✅ 多线程中在 `finally` 执行 `CountDownLatch.countDown()`，随机数使用 `ThreadLocalRandom`；时间戳使用 `System.currentTimeMillis()`。
- ✅ 使用 4 空格、K&R 大括号和 120 字符行宽；模块既有前端格式规则优先于该 Java 规则。
- ✅ Spring Bean 使用构造器注入；Web 层提供 `@RestControllerAdvice` 全局异常处理；Controller 使用 `@Validated` 和 DTO/VO 校验注解。
- ✅ `@Transactional` 限定在最小合理的 Service 层边界；业务异常映射为一致的 HTTP 状态与响应体，并为跨资源操作设计补偿或回滚。

### Vue 3 与 Pinia

- ✅ Vue 组件使用 `<script setup lang="ts">`、`ref()`、类型化 `defineProps` 与 `defineEmits`。
- ✅ 组件保持单一职责；超过 300 行或模板过于复杂时拆分子组件；卸载时清理事件监听、定时器和 watcher。
- ✅ 第三方非响应式实例使用 `readonly` 或 `shallowRef()` 包装；样式使用 CSS 变量和 `scoped`，避免全局污染。
- ✅ Pinia 使用 Setup Store；组件读取响应式状态使用 `storeToRefs()`，退出登录时清理敏感 Store 数据。
