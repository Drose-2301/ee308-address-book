# EE308 极限编程作业：通讯录（书签联系人 / 多联系方式 / Excel 导入导出）

> 说明：这是一份**共享稿**。作业要求每人各发一篇博客，两人可以在本稿基础上核对数字后，各自用自己账号发布，并在开头补上自己的姓名与分工。

| 项目 | 内容 |
| --- | --- |
| 课程 | [2601_FZU-MU_SE](https://bbs.csdn.net/forums/2601_MU_SE_FZU) |
| 团队名称 | （填写队名，例如 Team 7） |
| 作业要求 | [Extreme Programming（Address Book Application）](https://bbs.csdn.net/topics/620593436) |
| 本作业目标 | 实现书签联系人、多种联系方式、Excel 导入导出，并实践结对协作、Git 分支与 PR 流程 |
| 其他参考 | [Vue 3 官方文档](https://cn.vuejs.org/)、[Spring Boot 文档](https://docs.spring.io/spring-boot/)、[Apache POI 文档](https://poi.apache.org/)、[Vite 文档](https://cn.vitejs.dev/) |

## 目录

- [一、项目链接](#一项目链接)
- [二、分工与贡献比](#二分工与贡献比)
- [三、Git 协作与提交证据](#三git-协作与提交证据)
- [四、实现设计](#四实现设计)
  - [4.1 总体架构](#41-总体架构)
  - [4.2 数据模型](#42-数据模型)
  - [4.3 书签联系人](#43-书签联系人)
  - [4.4 多种联系方式](#44-多种联系方式)
  - [4.5 Excel 导出](#45-excel-导出)
  - [4.6 Excel 导入](#46-excel-导入)
  - [4.7 接口一览](#47-接口一览)
- [五、程序运行证据](#五程序运行证据)
- [六、协作中遇到的问题与解决](#六协作中遇到的问题与解决)
- [七、PSP 表](#七psp-表)

---

## 一、项目链接

| 内容 | 链接 |
| --- | --- |
| GitHub 团队仓库 | https://github.com/Drose-2301/ee308-address-book |
| 代码规范文档 | https://github.com/Drose-2301/ee308-address-book/blob/main/codestyle.md |
| 发布版本 v1.0.1 | https://github.com/Drose-2301/ee308-address-book/releases/tag/v1.0.1 |
| PR #1（发布准备） | https://github.com/Drose-2301/ee308-address-book/pull/1 |
| PR #2（后端去重与测试） | https://github.com/Drose-2301/ee308-address-book/pull/2 |
| PR #3（根路径与 404 修复） | https://github.com/Drose-2301/ee308-address-book/pull/3 |
| 演示视频 | （填写视频链接，例如 B 站） |
| 演示用导入文件 | 仓库内 `docs/demo-import.xlsx` |

技术栈：**Vue 3 + Vite**（前端） / **Spring Boot 4 + Spring Data JPA + SQLite + Apache POI 5**（后端）。

---

## 二、分工与贡献比

| 成员 | GitHub | 负责内容 | 贡献比 |
| --- | --- | --- | ---: |
| 黄俊涵 | [Drose-2301](https://github.com/Drose-2301) | 前端界面（列表 / 搜索 / 书签 / 新建与编辑弹窗）、Excel 交互接入、README 与代码规范、截图与演示视频、Release 与 PR 合并 | 60% |
| 队友（CountFish） | [1iu-cell](https://github.com/1iu-cell) | 后端实体与接口、Excel 导入导出实现、导入去重逻辑、后端测试（`ContactControllerTest` / `ContactServiceTest`） | 40% |

> 提交前请两人确认贡献比数字，合计必须为 100%，且两篇博客一致。

---

## 三、Git 协作与提交证据

### 3.1 提交记录

![GitHub 提交历史](screenshots/git-commits.png)

<!-- 上传说明：在 GitHub 仓库首页点 "Commits" 截图，或用 `git log --oneline --graph` 截图替代 -->

`main` 分支共 **10 次提交**，其中 3 次为 PR 合并提交：

| 提交 | 作者 | 内容 |
| --- | --- | --- |
| `766155a` | Junhan Huang | 加入演示用导入文件 |
| `d13883f` | Drose-2301 | 合并 PR #3 |
| `15c131f` | Junhan Huang | 根路径返回服务信息、未知路径返回 404 |
| `da45345` | Drose-2301 | 合并 PR #2 |
| `cf8d475` | **CountFish** | Excel 导入去重 + 后端测试 |
| `e76f6e1` | Drose-2301 | 合并 PR #1 |
| `b02b118` | Junhan Huang | CHANGELOG |
| `280f841` | Junhan Huang | README / 代码规范 / 截图 |
| `a02bfb6` | Junhan Huang | 前端界面实现 |
| `e0d7b41` | **CountFish** | 后端 CRUD + 书签 + Excel 导入导出 |

### 3.2 每人提交数

![按作者统计提交数](screenshots/git-shortlog.png)

```text
$ git shortlog -sn main
     8  Junhan Huang（5 次内容提交 + 3 次 PR 合并提交）
     2  CountFish（1iu-cell）
```

按**实质性代码 / 文档提交**统计为 **5 : 2**；若把 3 次 Pull Request 合并提交也算进去，则为 **8 : 2**。

### 3.3 协作流程

1. 先约定 `main` 为保护分支，所有改动走 `feat/*` 分支 + PR，至少一人 Review 后合并；
2. 提交信息统一为 `<type>: <摘要>`（`feat` / `fix` / `docs` / `test`），一次提交只做一件事；
3. 合并后删除临时分支，保持远端只有 `main`；
4. 每次合并后打 tag 并写 Release note，评审可直接对比版本差异。

---

## 四、实现设计

### 4.1 总体架构

前后端分离，前端只通过 `/api` 相对路径访问后端，开发时由 Vite 代理转发，部署时换成 Nginx 反代即可，不改代码。

```text
浏览器 (Vue 3)
   │  fetch /api/**
   ▼
Vite Dev Server (5173) ──proxy──▶ Spring Boot (8080)
                                    │
                                    ├─ Controller：参数绑定、HTTP 语义
                                    ├─ Service：事务与业务规则（去重、书签）
                                    ├─ Repository：Spring Data JPA
                                    └─ SQLite：addressbook.db（单文件）
```

分层原则：Controller 不写业务判断；Service 负责事务；对外统一返回 DTO（Java record），不直接序列化 JPA 实体，避免懒加载与双向引用问题。

### 4.2 数据模型

一名联系人拥有 0..N 条联系方式，用一对多关系表达“多种联系方式”：

```text
contacts                          contact_methods
─────────────                     ─────────────────────
id            PK                  id           PK
name          NOT NULL            contact_id   FK → contacts.id (CASCADE)
company                           type         PHONE/EMAIL/WECHAT/QQ/OTHER
position                          label        "工作手机"、"校园邮箱"
address                           value        号码 / 邮箱 / 账号
notes
favorite      BOOLEAN  ← 书签功能
created_at / updated_at
```

- `@OneToMany(cascade = ALL, orphanRemoval = true)`：删除联系人时级联删除其联系方式；
- 更新时采用“清空 + 重建”子集合的方式，保证顺序与前端提交一致；
- `favorite` 建在联系人上，而不是单独一张收藏表——书签只是联系人的一个状态位。

### 4.3 书签联系人

**后端**：`PATCH /api/contacts/{id}/favorite`，请求体 `{"favorite": true}`，只改一个字段，避免整对象提交覆盖其他修改。

**查询**：列表接口一条 JPQL 同时完成搜索、筛选与排序：

```java
select distinct c from Contact c left join c.methods m
where (:keyword = '' or lower(c.name) like :keyword
       or lower(coalesce(c.company, '')) like :keyword
       or lower(coalesce(c.position, '')) like :keyword
       or lower(coalesce(m.value, '')) like :keyword)
  and (:favoriteOnly = false or c.favorite = true)
order by c.favorite desc, c.name asc
```

`order by c.favorite desc` 让书签永久置顶；`left join c.methods` 让搜索能命中任意一个电话号码或邮箱。

**前端**：卡片右上角 ★ 直接调用切换接口后重新拉取列表，书签数由 `/api/contacts/stats` 提供；「只看书签」只是把 `favorite=true` 传给同一个列表接口，不需要新的后端逻辑。

### 4.4 多种联系方式

- 前端表单是一个可增删的数组，每行 = 类型下拉框 + 备注名 + 值，保存时整体提交；
- 后端 `ContactRequest.methods` 是数组，Service 逐条转成 `ContactMethod` 并挂到联系人上；
- 类型用枚举 `MethodType` 固定为五类，避免大小写与中英文不一致；备注名（label）可选，用来区分“手机 / 座机 / 工作邮箱”；
- 卡片上最多展示 3 条，其余折叠为“还有 N 项联系方式…”，避免卡片过高。

### 4.5 Excel 导出

用 Apache POI 的 `XSSFWorkbook` 生成真正的 `.xlsx`（不是 CSV 改后缀）：

| 姓名 | 公司 | 职位 | 书签 | 地址 | 备注 | 电话 | 邮箱 | 微信 | QQ | 其他联系方式 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 林晓 | 福大科技 | 产品经理 | 是 | 福州市… | 项目对接人 | 138 0013 8000 | linxiao@fzu-tech.com | linxiao_pm | | |

关键点：

1. **一行一人、一类一列**：符合“每类信息使用合适的列”的要求；
2. **同类型多值合并**：一个联系人有两个电话时，写在同一格里用 `；` 分隔，既不撑行也能完整表达；
3. 表头加粗白字 + 靛蓝底、冻结首行、按内容设置列宽，打开即可读；
4. 下载响应带 `Content-Disposition: attachment; filename*=UTF-8''...`，中文文件名不乱码。

### 4.6 Excel 导入

导入是整个作业里最容易“看起来能用、实际很脆”的部分，我们做了四层容错：

1. **格式兼容**：`WorkbookFactory.create()` 同时支持 `.xlsx` 与 `.xls`；
2. **表头别名 + 列顺序无关**：表头先归一化（去空格、转小写），再与别名表匹配，例如 `手机 / 电话 / phone / mobile / tel` 都映射到 `phone` 列；缺“姓名”列时直接给出可读错误；
3. **单元格读取**：统一用 `DataFormatter` 取值，避免电话号码被 Excel 当数字读成 `1.38E+10`；
4. **逐行容错**：
   - 整行为空 → 跳过，不计入统计；
   - 姓名为空 → 跳过，并在 `ImportResult.messages` 里写“第 N 行：姓名为空，已跳过”；
   - 同一单元格多个值（`；` / `、` / `;` / 换行）→ 拆成多条联系方式；
   - **去重**：以「姓名 + 首要联系方式（优先电话，其次任意联系方式）」为键，库中已有或文件内重复的行跳过，并提示行号。

导入返回结构：

```json
{
  "totalRows": 3,
  "imported": 2,
  "skipped": 1,
  "messages": ["第 3 行：姓名为空，已跳过"]
}
```

前端拿到后弹一条提示：“导入完成：共 3 行，成功 2 条，跳过 1 行”。

### 4.7 接口一览

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/contacts?keyword=&favorite=` | 列表 / 搜索 / 只看书签（书签置顶） |
| GET | `/api/contacts/{id}` | 单个联系人 |
| POST | `/api/contacts` | 新建（含 methods 数组） |
| PUT | `/api/contacts/{id}` | 全量更新 |
| PATCH | `/api/contacts/{id}/favorite` | 切换书签 |
| DELETE | `/api/contacts/{id}` | 删除（级联删联系方式） |
| GET | `/api/contacts/stats` | `{total, favorites}` |
| GET | `/api/contacts/export` | 下载 `.xlsx` |
| POST | `/api/contacts/import` | multipart 字段 `file` |

后端启动后访问 `http://localhost:8080/` 会返回接口清单，方便联调。

---

## 五、程序运行证据

### 5.1 界面截图

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

> CSDN 不支持仓库相对路径，发布时请把这 8 张图分别上传到 CSDN 图床后替换链接；图片文件在仓库 `screenshots/` 目录。

### 5.2 演示视频

（在此嵌入视频：上传到 B 站后插入 iframe，或直接用 CSDN 的视频功能上传本地 mp4）

演示内容：书签切换与筛选 → 给联系人增加一种联系方式 → 新建联系人 → 导出 Excel → 导入含空姓名行的 Excel（提示“跳过 1 行”）→ 搜索 → 后端接口页。

### 5.3 测试结果

```text
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| 测试类 | 用例数 | 覆盖内容 |
| --- | ---: | --- |
| `AddressbookBackendApplicationTests` | 1 | Spring 上下文 + SQLite/JPA 映射 |
| `ExcelServiceTest` | 2 | 导出 → 再导入往返、无联系方式联系人 |
| `ContactControllerTest` | 7 | 列表、筛选参数、新建 201、姓名校验 400、书签切换、删除 204、统计 |
| `ContactServiceTest` | 2 | 与库内重复、文件内重复的去重行为 |

### 5.4 实际导入返回

```json
{"totalRows":3,"imported":2,"skipped":1,"messages":["第 3 行：姓名为空，已跳过"]}
```

---

## 六、协作中遇到的问题与解决

| # | 问题 | 现象 | 解决 |
| --- | --- | --- | --- |
| 1 | 校园网访问 GitHub | `git push` 报 `Recv failure: Connection was reset`，`github.com:443` 直连被重置 | 改用 **SSH over 443**（`ssh.github.com:443`）+ 仓库 Deploy Key，并在 `~/.ssh/config` 固定 Host/Port |
| 2 | 电话号码被 Excel 当数字 | 导入后电话变成 `1.38E+10`，长号码末位变 0 | 统一用 `DataFormatter` 读单元格；导出时按字符串写入 |
| 3 | 重复导入产生重复联系人 | 同一份 Excel 导两次，列表出现两份 | 以「姓名 + 首要联系方式」为键去重，并提示“第 N 行：与已有联系人重复” |
| 4 | 打开后端根路径报 500 | 浏览器访问 `localhost:8080` 显示“服务器内部错误” | 原因是兜底 `@ExceptionHandler(Exception.class)` 把 `NoResourceFoundException` 也吞了；单独处理该异常返回 404，并新增 `/` 接口说明页 |
| 5 | 前后端联调跨域 | 5173 调 8080 被浏览器拦截 | 开发期用 Vite `server.proxy` 转发 `/api`，后端同时配置 CORS 兜底 |
| 6 | 两人改动互相覆盖 | 同时改 `ContactService` 时冲突 | 约定分支粒度：后端改动只在 `feat/*` 分支、前端只在 `feat/ui-*` 分支，合并前先 `git pull --rebase` |

---

## 七、PSP 表

### 7.1 黄俊涵（前端 / 文档 / 发布）

| 阶段 | 预估耗时（分钟） | 实际耗时（分钟） |
| --- | ---: | ---: |
| 需求分析（读作业要求、拆分功能点） | 20 | 25 |
| 设计（页面结构、组件划分、接口约定） | 30 | 40 |
| 编码（列表 / 搜索 / 书签 / 弹窗 / 导入导出接入） | 180 | 210 |
| 测试（本地联调、截图核对、异常场景） | 40 | 55 |
| 文档（README、代码规范、CHANGELOG、提交说明） | 60 | 75 |
| 发布（分支、PR、Release、仓库整理） | 20 | 30 |
| **合计** | **350** | **435** |

### 7.2 CountFish（后端）

| 阶段 | 预估耗时（分钟） | 实际耗时（分钟） |
| --- | ---: | ---: |
| 需求分析（确认接口与数据结构） | 20 | 20 |
| 设计（实体关系、Excel 列设计、去重键） | 30 | 35 |
| 编码（CRUD、书签、导入导出、去重） | 200 | 240 |
| 测试（`@WebMvcTest`、Service 单测、导入样例） | 60 | 80 |
| 文档（接口说明、后端交接说明） | 30 | 35 |
| **合计** | **340** | **410** |

---

## 八、小结

这次作业里最花时间的不是页面，而是两个边界：**Excel 的脏数据**和**两人并行改同一份代码**。前者靠“逐行容错 + 明确提示”而不是“整份拒绝”解决；后者靠“小分支 + PR + 固定提交信息”解决。功能上三项要求都已实现并有测试与截图佐证，代码与文档都放在团队仓库中，可复现构建与运行。
