# Member Responsibilities

Replace Member A-E with real names before using this in the report.

## Member A - Project Integration and Reservation

Main responsibility:

- project setup and integration;
- web server startup and login flow;
- reservation request workflow, including selecting multiple equipment items;
- consumable needs submitted with a reservation;
- approval/rejection workflow;
- run instructions and demo coordination.

Files to read:

- `pom.xml`
- `README.md`
- `run.sh`
- `src/main/java/edu/ucd/comp2013j/lab/App.java`
- `src/main/java/edu/ucd/comp2013j/lab/web/LabWebServer.java`
- `src/main/resources/web/app.js`
- `src/main/java/edu/ucd/comp2013j/lab/dao/ReservationDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/ReservationService.java`
- `docs/member-guides/member-a-reservation-integration.md`

Must be able to explain:

- how to run the project;
- how a reservation request is created;
- how single-equipment fixed-slot reservations are checked;
- how overlapping time is checked for each selected equipment item;
- how reservation consumable requests are stored;
- why transaction and `FOR UPDATE` are used.

## Member B - Database Design

Main responsibility:

- ER model;
- relational schema;
- SQL constraints;
- many-to-many link tables;
- reservation consumable request table;
- views and indexes;
- seed data;
- database design part of report.

Files to read:

- `src/main/resources/db/schema.sql`
- `src/main/resources/db/seed.sql`
- `src/main/java/edu/ucd/comp2013j/lab/db/Database.java`
- `docs/03-uml-modeling.md`
- `docs/04-database-design.md`
- `docs/member-guides/member-b-database-design.md`

Must be able to explain:

- all main tables;
- many-to-many relationships;
- primary keys and foreign keys;
- constraints and views;
- why `equipment_course_access` and `reservation_consumables` are separate tables;
- why approvals and stock transactions are separate tables.

## Member C - Equipment and Maintenance

Main responsibility:

- equipment catalogue;
- equipment search;
- admin equipment create/edit;
- equipment retirement instead of physical delete;
- maintenance ticket creation;
- ticket assignment/update;
- equipment status changes.

Files to read:

- `src/main/resources/web/index.html`
- `src/main/resources/web/app.js`
- `src/main/java/edu/ucd/comp2013j/lab/dao/EquipmentDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/MaintenanceDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/MaintenanceService.java`
- `docs/member-guides/member-c-equipment-maintenance.md`

Must be able to explain:

- what happens when equipment is reported broken;
- why retired equipment is kept for historical reservations and tickets;
- why the equipment status changes to `MAINTENANCE`;
- how a technician resolves a ticket;
- why ticket updates are stored separately.

## Member D - Inventory and Reports

Main responsibility:

- consumable inventory;
- creating a new consumable item type;
- stock transaction history;
- negative stock prevention;
- report queries and report UI.

Files to read:

- `src/main/resources/web/index.html`
- `src/main/resources/web/app.js`
- `src/main/java/edu/ucd/comp2013j/lab/dao/InventoryDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/ReportDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/InventoryService.java`
- `docs/member-guides/member-d-inventory-reports.md`

Must be able to explain:

- why there is a `stock_transactions` table;
- the difference between adding a consumable type and changing stock;
- how negative stock is prevented;
- what reports are shown;
- how SQL aggregation is used.

## Member E - Web Front-End, Testing, and Documentation

Main responsibility:

- HTML/CSS/JavaScript front-end;
- web page layout and browser interactions;
- role-based navigation;
- student self-registration;
- collapsed equipment picker and selected equipment summary;
- model classes;
- login support;
- automated tests;
- report/video/document organisation.

Files to read:

- `src/main/resources/web/index.html`
- `src/main/resources/web/style.css`
- `src/main/resources/web/app.js`
- `src/main/java/edu/ucd/comp2013j/lab/web/FormData.java`
- `src/main/java/edu/ucd/comp2013j/lab/web/Json.java`
- `src/main/java/edu/ucd/comp2013j/lab/model/*.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/UserDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/CourseDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/AuthService.java`
- `src/test/java/edu/ucd/comp2013j/lab/SystemServiceTest.java`
- `READ_04_FRONTEND_UI_GUIDE.md`
- `docs/member-guides/member-e-ui-testing-docs.md`

Must be able to explain:

- why the front-end is a web page;
- how JavaScript calls Java API endpoints;
- why different roles see different tabs;
- why self-registration only creates student accounts;
- why model classes are used;
- what automated tests cover;
- what screenshots are needed for the report.
