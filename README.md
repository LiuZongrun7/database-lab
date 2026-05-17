# Campus Laboratory Equipment Reservation and Maintenance System

This is a Java + JDBC database coursework project for COMP2013J. The system manages laboratory equipment reservations, approval decisions, maintenance tickets, consumable stock, and simple management reports.

The project includes a Web front-end built with HTML, CSS, and JavaScript. The Java back-end uses a lightweight HTTP server, JDBC, and a MySQL database.

## Read First

Team members should read these root-level guides first:

- `READ_00_DOCUMENT_MAP.md`
- `READ_01_PROJECT_OVERVIEW.md`
- `READ_02_MEMBER_RESPONSIBILITIES.md`
- `READ_03_VIVA_QA.md`
- `READ_04_FRONTEND_UI_GUIDE.md`
- `READ_05_SUBMISSION_AND_REPORT_GUIDE.md`
- `READ_CN_06_代码阅读与注释指南.md`
- `READ_CN_07_服务器部署指南.md`

## Run

```bash
DB_PASSWORD='your_mysql_root_password' mvn exec:java
```

Then open:

```text
http://localhost:8080
```

The application connects to local MySQL. On first start it creates the `lab_equipment` database if needed, recreates the tables, and inserts the seed data from `schema.sql` and `seed.sql`.

Default database settings:

| Setting | Value |
| --- | --- |
| URL | `jdbc:mysql://localhost:3306/lab_equipment` |
| User | `root` |
| Password | read from `DB_PASSWORD` |

You can override the connection with `DB_URL`, `DB_USER`, and `DB_PASSWORD`.
For server deployment, see `READ_CN_07_服务器部署指南.md`. If `DB_INIT_MODE=none`,
the app starts without recreating the database.

## Demo Accounts

| Username | Password | Role |
| --- | --- | --- |
| admin | admin123 | ADMIN |
| teacher | teacher123 | TEACHER |
| student1 | student123 | STUDENT |
| tech | tech123 | TECHNICIAN |

## Folders

- `src/main/java` - Java source code.
- `src/main/resources/db` - database schema and seed data.
- `docs` - coursework documents and diagrams.
- `docs/member-guides` - individual understanding and viva preparation notes.
