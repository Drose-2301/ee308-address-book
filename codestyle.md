# 代码规范（Code Style）

本项目的代码规范由两位成员在编码前共同约定，前端、后端分别遵守，提交前互相 Review。

## 通用

1. **命名**：类名 `UpperCamelCase`，方法 / 变量 `lowerCamelCase`，常量 `UPPER_SNAKE_CASE`；不使用拼音缩写（`lx`、`dh` 之类），除非是领域通用词（`qq`、`wechat`）。
2. **提交信息**：`<type>: <摘要>`，type 取 `feat` / `fix` / `refactor` / `docs` / `style` / `test` / `chore`，例如
   `feat: 支持为一个联系人添加多条电话号码`。
3. **一次提交只做一件事**：功能、格式化、依赖升级不混在同一个 commit 里。
4. **不提交生成物**：`node_modules/`、`dist/`、`target/`、`*.db` 均写入 `.gitignore`。
5. **中文注释只解释“为什么”**，不复述代码；公共方法必须写清输入、输出与异常。

## 后端（Java / Spring Boot）

1. 分层：`controller` 只做参数绑定与 HTTP 语义，`service` 负责事务与业务规则，`repository` 只写查询。控制器里不出现业务判断。
2. 对外一律返回 **DTO（record）**，不直接序列化 JPA 实体，避免懒加载和双向引用问题。
3. 依赖注入使用**构造器注入**，字段不加 `@Autowired`。
4. 事务：写操作由 `@Transactional` 方法包裹，只读查询标注 `readOnly = true`。
5. 异常：业务异常使用 `BadRequestException`（400）与 `NotFoundException`（404），由 `ApiExceptionHandler` 统一转换为 `{"message": "..."}`，前端直接展示该文案。
6. 数据库结构由 JPA 根据实体生成（`ddl-auto=update`），实体字段变更需同步 README 中的 Excel 列说明。
7. 单元/集成测试放在 `src/test/java`，命名 `XxxTests`。

## 前端（Vue 3）

1. 统一使用 `<script setup>` + 组合式 API，不写 Options API。
2. **所有网络请求集中在 `src/api.js`**，组件内不直接 `fetch`；错误统一抛出 `Error(message)`。
3. 组件职责单一：`App.vue` 负责页面状态与数据加载，`ContactCard.vue` 只负责展示，`ContactDialog.vue` 只负责表单与保存。
4. 事件命名使用 kebab-case（`@toggle-favorite`），`defineProps` / `defineEmits` 必须声明类型与默认值。
5. 样式使用 `<style scoped>`，颜色、圆角等统一取自 `style.css` 中的 CSS 变量，不在组件里硬编码新的主题色。
6. 不引入不必要的依赖：状态管理用 `ref` / `computed` 足够，不额外引入 Pinia / Vuex。

## Excel 约定

1. 列顺序固定为：姓名、公司、职位、书签、地址、备注、电话、邮箱、微信、QQ、其他联系方式。
2. 单元格内的多值分隔符统一使用中文分号 `；`。
3. 导入逻辑必须容错：表头别名、空行、缺列、姓名缺失等问题都不能让整个导入失败，能导入的行照常导入并返回提示。
