# EE308 Extreme Programming: Address Book (Bookmarks, Multiple Contact Methods, Excel Import/Export)

> This is a **shared draft**. The assignment requires each student to publish an individual blog post, so each teammate should copy this document, replace the "Author" line and the wording with their own, verify the numbers together, and publish it from their own CSDN account.

| Item | Content |
| --- | --- |
| Course for This Assignment | [2601_FZU-MU_SE](https://bbs.csdn.net/forums/2601_MU_SE_FZU) |
| Team Name | （team name, e.g. Team 7） |
| Assignment Requirements | [Extreme Programming (Address Book Application)](https://bbs.csdn.net/topics/620593436) |
| Objectives of This Assignment | Implement bookmark contacts, multiple contact methods and Excel import/export, and practise pair collaboration with Git branches and pull requests |
| Other References | [Vue 3 docs](https://vuejs.org/), [Spring Boot docs](https://docs.spring.io/spring-boot/), [Apache POI docs](https://poi.apache.org/), [Vite docs](https://vite.dev/) |

**Author:** （your name / student ID）

## Table of Contents

- [1. Project Links](#1-project-links)
- [2. Division of Work and Contribution Ratio](#2-division-of-work-and-contribution-ratio)
- [3. Git Collaboration and Commit Evidence](#3-git-collaboration-and-commit-evidence)
- [4. Implementation Design](#4-implementation-design)
  - [4.1 Overall Architecture](#41-overall-architecture)
  - [4.2 Data Model](#42-data-model)
  - [4.3 Bookmark Contacts](#43-bookmark-contacts)
  - [4.4 Multiple Contact Methods](#44-multiple-contact-methods)
  - [4.5 Excel Export](#45-excel-export)
  - [4.6 Excel Import](#46-excel-import)
  - [4.7 API Reference](#47-api-reference)
- [5. Program Evidence](#5-program-evidence)
- [6. Problems We Hit and How We Solved Them](#6-problems-we-hit-and-how-we-solved-them)
- [7. PSP Tables](#7-psp-tables)

---

## 1. Project Links

| Item | Link |
| --- | --- |
| GitHub team repository | https://github.com/Drose-2301/ee308-address-book |
| Code style document | https://github.com/Drose-2301/ee308-address-book/blob/main/codestyle.md |
| Release v1.0.1 | https://github.com/Drose-2301/ee308-address-book/releases/tag/v1.0.1 |
| PR #1 (release preparation) | https://github.com/Drose-2301/ee308-address-book/pull/1 |
| PR #2 (backend de-duplication and tests) | https://github.com/Drose-2301/ee308-address-book/pull/2 |
| PR #3 (root endpoint and 404 fix) | https://github.com/Drose-2301/ee308-address-book/pull/3 |
| Demo video | （link to the video, e.g. Bilibili） |
| Demo import file | `docs/demo-import.xlsx` in the repository |

Tech stack: **Vue 3 + Vite** (front end) and **Spring Boot 4 + Spring Data JPA + SQLite + Apache POI 5** (back end).

The application UI is in Chinese because the course is taught in Chinese; the code, comments and this document are in English.

---

## 2. Division of Work and Contribution Ratio

| Member | GitHub | Responsibilities | Contribution |
| --- | --- | --- | ---: |
| Junhan Huang | [Drose-2301](https://github.com/Drose-2301) | Front-end UI (list, search, bookmarks, create/edit dialog), Excel wiring in the UI, README and code style, screenshots and demo video, releases and PR merges | 60% |
| CountFish | [1iu-cell](https://github.com/1iu-cell) | Back-end entities and REST API, Excel import/export implementation, import de-duplication, back-end tests (`ContactControllerTest`, `ContactServiceTest`) | 40% |

> Both members must confirm these numbers before publishing; the two percentages must add up to 100% and be identical in both blog posts.

---

## 3. Git Collaboration and Commit Evidence

### 3.1 Commit history

![Git commit history](screenshots/git-commits.png)

`main` contains **10 commits**, 3 of which are pull-request merge commits:

| Commit | Author | Summary |
| --- | --- | --- |
| `766155a` | Junhan Huang | Add the demo import workbook |
| `d13883f` | Drose-2301 | Merge PR #3 |
| `15c131f` | Junhan Huang | Root endpoint returns service info, unknown paths return 404 |
| `da45345` | Drose-2301 | Merge PR #2 |
| `cf8d475` | **CountFish** | Excel import de-duplication + back-end tests |
| `e76f6e1` | Drose-2301 | Merge PR #1 |
| `b02b118` | Junhan Huang | CHANGELOG |
| `280f841` | Junhan Huang | README / code style / screenshots |
| `a02bfb6` | Junhan Huang | Front-end UI |
| `e0d7b41` | **CountFish** | Back-end CRUD, bookmarks and Excel import/export |

### 3.2 Commits per member

![Commits per author](screenshots/git-shortlog.png)

```text
$ git shortlog -sn main
     8  Junhan Huang   (5 content commits + 3 PR merge commits)
     2  CountFish      (1iu-cell)
```

Counting **substantive code/documentation commits only**, the split is **5 : 2**; including the three merge commits it is **8 : 2**.

### 3.3 How we collaborated

1. `main` is treated as protected: every change goes through a `feat/*` branch and a pull request, reviewed by the other member before merging.
2. Commit messages follow `<type>: <summary>` (`feat`, `fix`, `docs`, `test`), and one commit does one thing.
3. Merged branches are deleted immediately, so the remote only keeps `main`.
4. After each merge we create a tag and write release notes, so reviewers can diff released versions.

---

## 4. Implementation Design

### 4.1 Overall Architecture

The front end and back end are separated. The front end only ever calls relative `/api/**` paths; during development Vite proxies those calls to Spring Boot, and in production the same paths can be reverse-proxied by Nginx without touching the code.

```text
Browser (Vue 3)
   │  fetch /api/**
   ▼
Vite dev server (5173) ──proxy──▶ Spring Boot (8080)
                                    │
                                    ├─ Controller: parameter binding, HTTP semantics
                                    ├─ Service: transactions and business rules
                                    ├─ Repository: Spring Data JPA
                                    └─ SQLite: addressbook.db (single file)
```

Layering rules: controllers contain no business decisions; the service layer owns transactions; responses are DTOs (Java records) rather than JPA entities, which avoids lazy-loading and bidirectional-reference problems.

### 4.2 Data Model

A contact owns 0..N contact methods, which is how "multiple contact methods" is modelled:

```text
contacts                          contact_methods
─────────────                     ─────────────────────
id            PK                  id           PK
name          NOT NULL            contact_id   FK → contacts.id (CASCADE)
company                           type         PHONE/EMAIL/WECHAT/QQ/OTHER
position                          label        "work mobile", "campus email"
address                           value        number / address / handle
notes
favorite      BOOLEAN  ← bookmarks
created_at / updated_at
```

- `@OneToMany(cascade = ALL, orphanRemoval = true)`: deleting a contact deletes its methods;
- Updates rebuild the child collection ("clear + re-add"), so the order always matches what the form submitted;
- `favorite` lives on the contact instead of a separate favourites table — a bookmark is just a state of a contact, not an entity of its own.

### 4.3 Bookmark Contacts

**Back end:** `PATCH /api/contacts/{id}/favorite` with `{"favorite": true}`. A dedicated endpoint changes exactly one field, so toggling a bookmark can never overwrite other edits by accident.

**Query:** one JPQL statement handles search, filtering and ordering:

```java
select distinct c from Contact c left join c.methods m
where (:keyword = '' or lower(c.name) like :keyword
       or lower(coalesce(c.company, '')) like :keyword
       or lower(coalesce(c.position, '')) like :keyword
       or lower(coalesce(m.value, '')) like :keyword)
  and (:favoriteOnly = false or c.favorite = true)
order by c.favorite desc, c.name asc
```

`order by c.favorite desc` keeps bookmarked contacts pinned to the top, and `left join c.methods` lets the keyword match any phone number or email address.

**Front end:** the star in the card corner calls the toggle endpoint and reloads the list; the counters come from `/api/contacts/stats`. The "bookmarks only" filter simply passes `favorite=true` to the same list endpoint, so no extra back-end code is needed.

### 4.4 Multiple Contact Methods

- The form is an editable array: each row is a type selector, an optional label and a value, submitted as a whole;
- The back end accepts `ContactRequest.methods` as an array and maps each entry to a `ContactMethod` owned by the contact;
- Types are a fixed enum `MethodType` with five values, which avoids case and translation drift; the optional label distinguishes "mobile" from "landline" or "work email";
- Cards show at most three methods and collapse the rest into "N more methods…" so cards stay compact.

### 4.5 Excel Export

Export uses Apache POI's `XSSFWorkbook` to produce a real `.xlsx` file (not a renamed CSV):

| Name | Company | Title | Bookmark | Address | Notes | Phone | Email | WeChat | QQ | Other |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Lin Xiao | FZU Tech | Product manager | Yes | Fuzhou… | Main contact | 138 0013 8000 | linxiao@fzu-tech.com | linxiao_pm | | |

Key decisions:

1. **One row per contact, one column per kind of information**, which matches the requirement that each kind of information has its own labelled column;
2. **Multiple values of the same kind stay in one cell**, joined by `；`, so a contact with two phone numbers does not need two rows;
3. Styled header row (bold white on indigo), frozen first row and explicit column widths make the file readable as soon as it is opened;
4. The download response sets `Content-Disposition: attachment; filename*=UTF-8''…`, so the Chinese file name is not mangled.

### 4.6 Excel Import

Import is the part that is easy to make "work on my machine" and break on real data, so we built four layers of tolerance:

1. **Format support:** `WorkbookFactory.create()` accepts both `.xlsx` and `.xls`;
2. **Header aliases and free column order:** headers are normalised (spaces removed, lower-cased) and matched against an alias table, so `手机 / 电话 / phone / mobile / tel` all map to the `phone` column; a missing "name" column produces a readable error instead of a stack trace;
3. **Cell reading:** every value is read through `DataFormatter`, so a phone number stored as a number does not become `1.38E+10`;
4. **Row-by-row tolerance:**
   - an entirely empty row is skipped and not counted;
   - a row without a name is skipped and reported as "row N: name is empty, skipped";
   - several values in one cell (`；`, `、`, `;`, newline) are split into separate contact methods;
   - **de-duplication:** the key is "name + primary contact value (phone first, then any other method)"; rows that duplicate an existing contact or another row in the same file are skipped and reported with their row number.

The import response looks like this:

```json
{
  "totalRows": 3,
  "imported": 2,
  "skipped": 1,
  "messages": ["Row 3: name is empty, skipped"]
}
```

The UI turns it into a toast: "Import finished: 3 rows, 2 imported, 1 skipped".

### 4.7 API Reference

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/contacts?keyword=&favorite=` | List / search / bookmarks only (bookmarks first) |
| GET | `/api/contacts/{id}` | Single contact |
| POST | `/api/contacts` | Create a contact (with a `methods` array) |
| PUT | `/api/contacts/{id}` | Full update |
| PATCH | `/api/contacts/{id}/favorite` | Toggle the bookmark flag |
| DELETE | `/api/contacts/{id}` | Delete a contact (cascades to its methods) |
| GET | `/api/contacts/stats` | `{total, favorites}` |
| GET | `/api/contacts/export` | Download the `.xlsx` file |
| POST | `/api/contacts/import` | Multipart upload, field name `file` |

Opening `http://localhost:8080/` returns a small JSON index with the application information and the endpoint list.

---

## 5. Program Evidence

### 5.1 Screenshots

| Main screen (bookmarks pinned) | Bookmarks only |
| --- | --- |
| ![Main screen](screenshots/01-home.png) | ![Bookmarks only](screenshots/02-favorites.png) |

| Create contact (several methods) | Edit contact |
| --- | --- |
| ![Create](screenshots/03-create-dialog.png) | ![Edit](screenshots/04-edit-dialog.png) |

| Search | Bookmark toast |
| --- | --- |
| ![Search](screenshots/05-search.png) | ![Bookmark toast](screenshots/06-favorite-toast.png) |

| Excel export | Excel import |
| --- | --- |
| ![Export](screenshots/07-export.png) | ![Import](screenshots/08-import.png) |

> CSDN does not support repository-relative image paths. Upload the ten images in `screenshots/` to CSDN and replace the links before publishing.

### 5.2 Demo video

（Embed the video here: upload the mp4 to Bilibili and paste the iframe, or use the CSDN video uploader.）

The recording walks through: toggling and filtering bookmarks → adding one more contact method to an existing contact → creating a contact → exporting to Excel → importing a file whose third row has no name (the UI reports "1 skipped") → searching → the back-end endpoint index page.

### 5.3 Test results

```text
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

| Test class | Cases | Coverage |
| --- | ---: | --- |
| `AddressbookBackendApplicationTests` | 1 | Spring context and SQLite/JPA mapping |
| `ExcelServiceTest` | 2 | Export → import round trip; a contact without methods |
| `ContactControllerTest` | 7 | List, filter parameters, create 201, blank-name 400, bookmark toggle, delete 204, stats |
| `ContactServiceTest` | 2 | De-duplication against the database and inside one file |

### 5.4 A real import response

```json
{"totalRows":3,"imported":2,"skipped":1,"messages":["第 3 行：姓名为空，已跳过"]}
```

---

## 6. Problems We Hit and How We Solved Them

| # | Problem | Symptom | Solution |
| --- | --- | --- | --- |
| 1 | GitHub unreachable from the campus network | `git push` failed with `Recv failure: Connection was reset`; `github.com:443` was reset | Switched to **SSH over port 443** (`ssh.github.com:443`) with a repository deploy key, pinned in `~/.ssh/config` |
| 2 | Phone numbers treated as numbers by Excel | Imported numbers showed as `1.38E+10`, and long numbers lost their last digits | Read every cell through `DataFormatter`; write values as strings on export |
| 3 | Importing the same file twice created duplicates | The list showed two copies of the same contact | De-duplicate on "name + primary contact value" and report "row N: duplicates an existing contact" |
| 4 | The back-end root path returned 500 | Opening `localhost:8080` showed "internal server error" | The catch-all `@ExceptionHandler(Exception.class)` also swallowed `NoResourceFoundException`; we handle it separately as 404 and added a `/` index endpoint |
| 5 | Cross-origin calls between 5173 and 8080 | The browser blocked the requests during development | Vite `server.proxy` forwards `/api`, and the back end also configures CORS as a fallback |
| 6 | Both members editing the same file | Merge conflicts in `ContactService` | Agreed on branch ownership: back-end work in `feat/*`, front-end work in `feat/ui-*`, and `git pull --rebase` before merging |

---

## 7. PSP Tables

### 7.1 Junhan Huang (front end, documentation, release)

| Phase | Estimated (min) | Actual (min) |
| --- | ---: | ---: |
| Requirements analysis (read the assignment, split the features) | 20 | 25 |
| Design (page structure, components, API contract) | 30 | 40 |
| Coding (list, search, bookmarks, dialogs, import/export wiring) | 180 | 210 |
| Testing (local integration, screenshot review, edge cases) | 40 | 55 |
| Documentation (README, code style, CHANGELOG, submission notes) | 60 | 75 |
| Release (branches, PRs, releases, repository clean-up) | 20 | 30 |
| **Total** | **350** | **435** |

### 7.2 CountFish (back end)

| Phase | Estimated (min) | Actual (min) |
| --- | ---: | ---: |
| Requirements analysis (API and data structures) | 20 | 20 |
| Design (entity relations, Excel layout, de-duplication key) | 30 | 35 |
| Coding (CRUD, bookmarks, import/export, de-duplication) | 200 | 240 |
| Testing (`@WebMvcTest`, service unit tests, sample imports) | 60 | 80 |
| Documentation (API notes, back-end handover) | 30 | 35 |
| **Total** | **340** | **410** |

---

## 8. Summary

The hard parts of this assignment were not the pages but two boundaries: **dirty data in Excel** and **two people editing the same code base**. The first we solved with row-level tolerance plus explicit messages instead of rejecting the whole file; the second with small branches, pull requests and a fixed commit-message convention. All three required features are implemented, covered by tests and shown in screenshots and a demo video, and the repository builds and runs from a clean clone.
