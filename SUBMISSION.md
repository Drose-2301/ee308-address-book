# EE308 极限编程作业 — 提交说明

## 一、作业信息

| 项目 | 内容 |
| --- | --- |
| 课程 | EE308FZ Software Engineering — 2601_FZU-MU_SE |
| 作业 | Extreme Programming（Address Book Application） |
| 截止 | 2026-11-17 23:59（UTC+8） |
| 团队仓库 | https://github.com/Drose-2301/ee308-address-book |
| 发布版本 | https://github.com/Drose-2301/ee308-address-book/releases/tag/v1.0.0 |
| 已合并 PR | https://github.com/Drose-2301/ee308-address-book/pull/1 |
| 代码规范 | 仓库内 `codestyle.md` |

## 二、本压缩包内容

```
EE308-address-book/
├─ addressbook-b/addressbook-backend/   后端源码（Spring Boot 4 + JPA + SQLite + Apache POI）
├─ addressbook-f/addressbook-frontend/  前端源码（Vue 3 + Vite）
├─ docs/                                通讯录导入模板.xlsx、通讯录导出示例.xlsx
├─ screenshots/                         8 张运行截图（主界面 / 书签 / 新建 / 编辑 / 搜索 / 导出 / 导入）
├─ README.md                            功能对照、运行方式、接口列表、Excel 格式、测试说明
├─ codestyle.md                         团队约定的代码规范
├─ CHANGELOG.md                         版本记录（v1.0.0）
└─ SUBMISSION.md                        本文件
```

压缩包内**不含** `node_modules/`、`target/`、`dist/`、`*.db`，这些都是可重新生成的构建产物；
按下面“运行方式”一节即可从零构建。

## 三、运行方式

需要 JDK 17+、Node.js 18+。

```bash
# 1) 后端（默认 http://localhost:8080，首次启动自动建表并写入 5 条演示数据）
cd addressbook-b/addressbook-backend
./mvnw spring-boot:run            # Windows: mvnw.cmd spring-boot:run

# 2) 前端（默认 http://localhost:5173，已配置 /api 代理到 8080）
cd addressbook-f/addressbook-frontend
npm install
npm run dev
```

打包方式：

```bash
# 后端可执行 jar
cd addressbook-b/addressbook-backend && ./mvnw -DskipTests package
# 前端静态文件
cd addressbook-f/addressbook-frontend && npm run build
```

## 四、功能与评分对照

| 评分项 | 分值 | 实现说明 |
| --- | ---: | --- |
| 博客格式 / 仓库与规范链接 | 10 | 仓库、`codestyle.md` 均在本包与 GitHub 仓库中 |
| 实现思路、程序截图、分工 | 20 | `README.md` 含实现说明与截图；分工见第六节 |
| 页面样式 | 10 | 渐变头部、卡片网格、弹窗表单、空状态、Toast、响应式 |
| 书签联系人 | 25 | ★ 一键切换、书签置顶、顶部计数、“只看书签”筛选 |
| 多种联系方式 | 10 | 一个联系人 0..N 条（电话/邮箱/微信/QQ/其他），可自定义备注名 |
| Excel 导入导出 | 25 | POI 导出（一行一人、一类一列、多值以 `；` 合并）；导入支持表头别名、列顺序调整、空姓名行跳过、姓名+首要联系方式去重 |
| 云端部署（选做） | +15 | 未做 |

## 五、测试

```bash
cd addressbook-b/addressbook-backend
./mvnw test
```

共 12 个测试，全部通过：

| 测试类 | 用例数 | 覆盖内容 |
| --- | ---: | --- |
| `AddressbookBackendApplicationTests` | 1 | Spring 上下文 + SQLite/JPA 映射 |
| `ExcelServiceTest` | 2 | 导出 → 再导入往返、无联系方式联系人 |
| `ContactControllerTest` | 7 | 列表/筛选/新建/校验失败/书签切换/删除/统计的 HTTP 契约 |
| `ContactServiceTest` | 2 | 导入去重（与库内重复、文件内重复） |

## 六、分工与贡献比（按实际情况填写后使用）

| 成员 | GitHub | 负责内容 | 贡献比 |
| --- | --- | --- | ---: |
| 黄俊涵 | Drose-2301 | 前端界面（列表/搜索/书签/弹窗）、Excel 交互、文档与截图、发布与 PR | 65% |
| 队友 | 1iu-cell | 后端实体与接口、Excel 导入导出实现、后端测试 | 35% |

> 提交前请把姓名、贡献比改成你们双方确认后的数字，两边博客保持一致。

## 七、提交状态

- [x] GitHub 团队仓库（public）+ 至少 3 次提交
- [x] Release `v1.0.0`
- [x] Pull Request（#1，已合并）
- [x] 代码规范文档
- [x] 运行截图 + 示例 Excel
- [ ] 个人 CSDN 博客（每人各一篇，待发布）
- [ ] 演示视频（待录制）
- [ ] 云端部署（选做，未做）
