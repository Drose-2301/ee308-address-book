This post walks through a pair-programming assignment from our software engineering course: building an address book that supports bookmarked contacts, several contact methods per person, and Excel import/export. We describe how we split the work, how the data model is shaped, and the parts that actually took time — dirty Excel data and keeping two people's changes apart.

## Contents

1. What the application does
2. Division of work
3. How we used Git
4. Data model
5. Bookmarks
6. Excel export
7. Excel import
8. API surface
9. Screenshots
10. Demo video
11. Tests
12. Problems worth mentioning
13. PSP
14. Closing thoughts

## Assignment information

| Item | Content |
| --- | --- |
| Course | 2601_FZU-MU_SE |
| Team | （team name） |
| Assignment | Extreme Programming (Address Book Application) |
| Objectives | Implement bookmarks, multiple contact methods and Excel import/export; practise pair collaboration with Git |
| Author | （your name / student ID） |

The application UI is in Chinese because the course is taught in Chinese; the code, comments and this article are in English.

## 1. What the application does

The address book keeps a list of contacts. Each contact can be marked as a bookmark, which pins it to the top of the list and makes it reachable through a one-click filter. Each contact also owns an arbitrary number of contact methods — phone, email, WeChat, QQ or anything else — and every method can carry its own label such as "work mobile" or "campus email". The list can be exported to a real Excel workbook and loaded back from one.

Stack: Vue 3 with Vite on the front end; Spring Boot 4 with Spring Data JPA, SQLite and Apache POI on the back end. The two halves talk over a small JSON API under `/api`.

The repository is public and can be cloned directly:

```text
github.com/Drose-2301/ee308-address-book
```

## 2. Division of work

| Member | Responsibilities | Contribution |
| --- | --- | --- |
| Junhan Huang | Front-end UI (list, search, bookmarks, create/edit dialog), Excel wiring, documentation, screenshots, releases | 50% |
| CountFish | Back-end entities and API, Excel import/export, de-duplication, back-end tests | 50% |

## 3. How we used Git

We treated `main` as protected. Every change went through a feature branch and a pull request, and the other member reviewed it before merging. Commit messages follow a fixed pattern — `feat:`, `fix:`, `docs:`, `test:` — so the history reads like a changelog. Merged branches were deleted immediately, which keeps the branch list down to `main`.

The main branch holds ten commits, three of which are pull-request merges. Excluding merge commits, the split is five front-end/documentation commits and two back-end commits.

【Figure 1: commit history screenshot — insert here before publishing】

【Figure 2: commits-per-author screenshot — insert here before publishing】

## 4. Data model

The interesting design decision is modelling "multiple contact methods". Instead of fixed columns such as phone, phone2, email, email2 on the contact table, we gave contact methods their own table:

```text
contacts                          contact_methods
─────────────                     ─────────────────────
id            PK                  id           PK
name          NOT NULL            contact_id   FK → contacts.id (CASCADE)
company                           type         PHONE/EMAIL/WECHAT/QQ/OTHER
position                          label        "work mobile", "campus email"
address                           value        number / address / handle
notes
favorite      BOOLEAN
created_at / updated_at
```

`favorite` lives on the contact rather than in a separate favourites table, because a bookmark is a state of a contact, not an entity of its own. The one-to-many association uses `cascade = ALL` and `orphanRemoval = true`, so deleting a contact also removes its methods. On update the child collection is rebuilt from the request, which guarantees the stored order matches what the form showed.

## 5. Bookmarks

Toggling a bookmark is a dedicated endpoint, `PATCH /api/contacts/{id}/favorite`, carrying only the boolean. A narrower endpoint means a bookmark click can never overwrite a contact's other fields by accident.

Listing, searching, filtering and ordering are one query:

```java
select distinct c from Contact c left join c.methods m
where (:keyword = '' or lower(c.name) like :keyword
       or lower(coalesce(c.company, '')) like :keyword
       or lower(coalesce(c.position, '')) like :keyword
       or lower(coalesce(m.value, '')) like :keyword)
  and (:favoriteOnly = false or c.favorite = true)
order by c.favorite desc, c.name asc
```

`order by c.favorite desc` pins bookmarks to the top; the `left join` on methods lets a search term match a phone number or an email address, not just a name. The "bookmarks only" switch in the UI simply passes `favorite=true` to the same endpoint, so it needs no extra back-end code.

## 6. Excel export

Export uses Apache POI to build a genuine `.xlsx` workbook — one row per contact, one column per kind of information, which is what the assignment asks for. When a contact has two phone numbers, both values stay in the same cell separated by a full-width semicolon; that keeps the sheet one-row-per-person without losing data.

The header row is styled, the first row is frozen and column widths are set explicitly, so the file is readable the moment it is opened. The download response sets a UTF-8 encoded file name in `Content-Disposition`, otherwise the Chinese file name turns into mojibake in some browsers.

## 7. Excel import

Import is where a feature that "works on my machine" usually breaks on real files, so we built four layers of tolerance.

First, the format: `WorkbookFactory.create()` accepts both `.xlsx` and `.xls`.

Second, the headers: they are normalised (spaces stripped, lower-cased) and matched against an alias table, so "手机", "电话", "phone", "mobile" and "tel" all map to the phone column, and the column order does not matter. If the name column is missing, the user gets a readable message instead of a stack trace.

Third, the cells: every value is read through `DataFormatter`. Without it, a phone number stored as a number comes back as `1.38E+10` and the last digits are gone.

Fourth, the rows. An entirely empty row is skipped silently. A row without a name is skipped and reported with its row number. Several values in one cell are split on common separators into separate contact methods. And rows that duplicate an existing contact, or another row in the same file, are skipped too: the key is the name plus the primary contact value, with phone numbers normalised first.

The endpoint returns a small summary instead of a bare "ok":

```json
{
  "totalRows": 3,
  "imported": 2,
  "skipped": 1,
  "messages": ["Row 3: empty name, skipped"]
}
```

The UI turns that into a toast, so the user knows exactly how many rows made it and which ones did not.

## 8. API surface

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/contacts?keyword=&favorite=` | list, search, bookmarks only |
| POST | `/api/contacts` | create a contact with its methods |
| PUT | `/api/contacts/{id}` | update |
| PATCH | `/api/contacts/{id}/favorite` | toggle bookmark |
| DELETE | `/api/contacts/{id}` | delete, cascading to methods |
| GET | `/api/contacts/export` | download the workbook |
| POST | `/api/contacts/import` | upload a workbook |

## 9. Screenshots

【Figure 3: main screen with bookmarks pinned — insert here】

【Figure 4: bookmarks-only filter — insert here】

【Figure 5: create dialog with several contact methods — insert here】

【Figure 6: export completed — insert here】

【Figure 7: import result reporting one skipped row — insert here】

## 10. Demo video

【Insert the demo video with the CSDN video uploader; it is about one minute long and walks through bookmarking, adding a contact method, creating a contact, exporting, importing and searching.】

## 11. Tests

The back end has twelve tests, all passing:

```text
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

There is a context-loading test for the Spring + SQLite wiring, a round-trip test that exports a workbook and reads it back, seven web-layer tests for the controller (list, filters, create, validation failure, bookmark toggle, delete, stats) and two service tests for the de-duplication rules.

## 12. Problems worth mentioning

**GitHub was unreachable from the campus network.** Pushes failed with a connection reset on port 443. We switched to SSH over port 443 with a deploy key, which has been stable since.

**Phone numbers turned into scientific notation.** This was the `DataFormatter` issue described above; it only showed up when testing with numbers typed into Excel by hand.

**Importing the same file twice duplicated contacts.** The fix was the de-duplication key and the per-row message, which also made the import dialog far more useful.

**The back-end root path returned 500.** The catch-all exception handler was swallowing Spring's own "no static resource" exception and turning a 404 into a server error. Handling that exception separately fixed it, and a small index endpoint now makes the root path useful.

**Two people editing the same service class.** We split branch ownership by area and rebased before merging; conflicts dropped to almost nothing.

## 13. PSP

| Phase | Junhan Huang estimate | actual | CountFish estimate | actual |
| --- | ---: | ---: | ---: | ---: |
| Requirements | 20 | 25 | 20 | 20 |
| Design | 30 | 40 | 30 | 35 |
| Coding | 180 | 210 | 200 | 240 |
| Testing | 40 | 55 | 60 | 80 |
| Documentation | 60 | 75 | 30 | 35 |
| Release | 20 | 30 | — | — |
| **Total** | **350** | **435** | **340** | **410** |

## 14. Closing thoughts

The pages were the easy part. What took time was deciding how to represent several contact methods without painting ourselves into a corner, and how to make Excel import forgiving enough for files that people actually produce. Row-level tolerance plus explicit messages turned out to be much more valuable than strict validation that rejects the whole file.
