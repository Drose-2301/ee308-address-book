# 通讯录 Address Book（EE308 极限编程作业）

一个支持**书签联系人**、**多种联系方式**和**Excel 导入/导出**的通讯录应用，前后端分离：

| 层 | 技术 | 目录 |
| --- | --- | --- |
| 前端 | Vue 3 + Vite | `addressbook-f/addressbook-frontend` |
| 后端 | Spring Boot 4 (Java 17) + Spring Data JPA | `addressbook-b/addressbook-backend` |
| 数据库 | SQLite（单文件 `addressbook.db`，首次启动自动建表并写入演示数据） | — |
| Excel | Apache POI 5（读写真正的 `.xlsx`） | — |

## 功能对照作业要求

| 作业要求 | 实现 |
| --- | --- |
| 书签联系人（25 分） | 卡片右上角 ★ 一键切换；书签联系人置顶；顶部显示书签数量；支持“只看书签”筛选 |
| 多种联系方式（10 分） | 一个联系人拥有 0..N 条联系方式，类型为电话 / 邮箱 / 微信 / QQ / 其他，可自定义备注名（如“工作手机”“校园邮箱”） |
| Excel 导入导出（25 分） | 后端用 POI 导出带表头样式的 `.xlsx`；导入支持表头别名、多值单元格、逐行错误提示 |
| 界面 | 响应式卡片网格、搜索、空状态、表单弹窗、Toast 提示 |
| 协作 | Git 分模块提交（前端 / 后端 / 文档），代码规范见 `codestyle.md` |

## 界面预览

| 主界面（书签置顶） | 只看书签 |
| --- | --- |
| ![主界面](screenshots/01-home.png) | ![只看书签](screenshots/02-favorites.png) |

| 新建联系人（多条联系方式） | 编辑联系人 |
| --- | --- |
| ![新建](screenshots/03-create-dialog.png) | ![编辑](screenshots/04-edit-dialog.png) |

| 搜索 | 书签切换提示 |
| --- | --- |
| ![搜索](screenshots/05-search.png) | ![书签](screenshots/06-favorite-toast.png) |

| 导出 Excel | 导入 Excel |
| --- | --- |
| ![导出](screenshots/07-export.png) | ![导入](screenshots/08-import.png) |

## 目录结构

```
addressbook/
├─ addressbook-b/addressbook-backend/     # Spring Boot 后端
│  ├─ src/main/java/com/example/addressbook/
│  │  ├─ controller/     # REST 接口 + 统一异常处理
│  │  ├─ service/        # 业务逻辑（事务边界在这里）
│  │  ├─ excel/          # Apache POI 导入 / 导出
│  │  ├─ entity/         # Contact / ContactMethod / MethodType
│  │  ├─ repository/     # Spring Data JPA
│  │  ├─ dto/            # 请求 / 响应对象（record）
│  │  └─ config/         # CORS + 演示数据
│  └─ src/main/resources/application.properties
├─ addressbook-f/addressbook-frontend/    # Vue 3 + Vite 前端
│  └─ src/
│     ├─ api.js               # 所有 HTTP 调用集中在这里
│     ├─ methodTypes.js       # 联系方式类型的中文名与配色
│     ├─ components/ContactCard.vue
│     ├─ components/ContactDialog.vue
│     └─ App.vue              # 页面骨架、搜索、导入导出入口
└─ codestyle.md
```

## 运行方式

### 1. 启动后端（默认 http://localhost:8080）

```bash
cd addressbook-b/addressbook-backend
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

首次启动会在当前目录创建 `addressbook.db`，并写入 5 条演示联系人（其中 2 条为书签）。

### 2. 启动前端（默认 http://localhost:5173）

```bash
cd addressbook-f/addressbook-frontend
npm install
npm run dev
```

Vite 已配置代理：`/api` → `http://localhost:8080`，因此前端不需要写死后端地址。

### 3. 打包

```bash
# 后端：生成可执行 jar
./mvnw -DskipTests package
java -jar target/addressbook-backend-0.0.1-SNAPSHOT.jar

# 前端：生成静态文件到 dist/
npm run build
```

## REST 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/contacts?keyword=&favorite=false` | 列表 / 搜索 / 只看书签（书签置顶） |
| GET | `/api/contacts/{id}` | 单个联系人详情 |
| POST | `/api/contacts` | 新建联系人（含联系方式数组） |
| PUT | `/api/contacts/{id}` | 全量更新 |
| PATCH | `/api/contacts/{id}/favorite` | 切换书签，body：`{"favorite": true}` |
| DELETE | `/api/contacts/{id}` | 删除联系人（级联删除联系方式） |
| GET | `/api/contacts/stats` | `{total, favorites}` |
| GET | `/api/contacts/export` | 下载 `.xlsx` |
| POST | `/api/contacts/import` | `multipart/form-data`，字段名 `file` |

## Excel 格式

导出文件第 1 行为表头，一名联系人一行：

| 姓名 | 公司 | 职位 | 书签 | 地址 | 备注 | 电话 | 邮箱 | 微信 | QQ | 其他联系方式 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 林晓 | 福大科技有限公司 | 产品经理 | 是 | 福州市鼓楼区… | 项目对接人 | 138 0013 8000 | linxiao@fzu-tech.com | linxiao_pm | | |

- 同一类型的多条信息写在同一个单元格里，用 `；` 或 `、` 分隔（导入时也接受 `;`）。
- “书签”列接受 `是 / y / yes / true / 1`，其余视为否。
- 导入时表头支持别名（如 `手机` / `电话`、`邮箱` / `email`），列顺序可以调整；缺少“姓名”列会给出明确提示。
- 姓名为空的行会被跳过，并在返回结果的 `messages` 中列出具体行号。

附件：

- `docs/通讯录导入模板.xlsx` —— 带表头样式的空白模板（含两行示例，可删除后填写）。
- `docs/通讯录导出示例.xlsx` —— 由 `/api/contacts/export` 实际导出的文件，可直接打开查看格式。

## 测试

```bash
cd addressbook-b/addressbook-backend
./mvnw test
```

| 测试 | 覆盖内容 |
| --- | --- |
| `AddressbookBackendApplicationTests` | Spring 上下文、SQLite + JPA 映射能否正常启动 |
| `ExcelServiceTest#exportThenImportKeepsContactsAndMethods` | 导出 → 再导入的往返测试：姓名、书签、3 条联系方式（2 个电话 + 1 个邮箱）全部保留 |
| `ExcelServiceTest#contactWithoutMethodsRoundTrips` | 没有任何联系方式的联系人也能正常往返 |

此外用接口做过一次完整的端到端验证：`POST` 新建（6 条联系方式）→ `GET /export` 导出 →
`POST /import` 重新导入 → 行数、书签、多值单元格拆分均一致；导入含“姓名为空”行的表格时返回
`{"totalRows":3,"imported":2,"skipped":1,"messages":["第 4 行：姓名为空，已跳过"]}`。

## 说明

- 首次启动会自动写入 5 条演示数据（2 条书签），方便直接截图/演示；删除 `addressbook.db` 即可重置。
- 前端只通过 `/api` 相对路径访问后端，生产环境可把 `dist/` 交给 Nginx 托管并反向代理 `/api`，
  因此换服务器时不需要改代码。

