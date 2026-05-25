# Team Division Document

This document describes the planned division of work for a five-person group. The names are placeholders and should be replaced with the real team member names before submission.

## Member A - Project Integration and Reservation Module

Responsibilities:
- Coordinate weekly progress and final integration.
- Set up Maven project structure.
- Implement reservation workflow, including single-equipment fixed-slot requests.
- Connect reservation consumable needs to the reservation form and database.
- Write reservation-related service logic and tests.
- Prepare the final run instructions.

Main files:
- `pom.xml`
- `README.md`
- `src/main/java/edu/ucd/comp2013j/lab/App.java`
- `src/main/java/edu/ucd/comp2013j/lab/web/LabWebServer.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/ReservationDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/ReservationService.java`
- `src/main/resources/web/app.js`

## Member B - Database Design

Responsibilities:
- Design ER model and relational schema.
- Write table definitions, link tables, constraints, views, indexes, and seed data.
- Explain mapping from ER model to relational model.
- Check SQL statements in the report.

Main files:
- `src/main/resources/db/schema.sql`
- `src/main/resources/db/seed.sql`
- `src/main/java/edu/ucd/comp2013j/lab/db/Database.java`
- `docs/03-uml-modeling.md`
- `docs/04-database-design.md`

## Member C - Equipment and Maintenance Module

Responsibilities:
- Implement equipment catalogue.
- Implement admin equipment create/edit/retire operations.
- Implement problem reporting and maintenance ticket handling.
- Connect maintenance status with equipment availability.
- Prepare maintenance demo scenario.

Main files:
- `src/main/java/edu/ucd/comp2013j/lab/dao/EquipmentDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/MaintenanceDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/MaintenanceService.java`
- `src/main/resources/web/index.html`
- `src/main/resources/web/app.js`

## Member D - Inventory and Reports Module

Responsibilities:
- Implement consumable inventory management.
- Implement creating new consumable item types.
- Implement stock transaction logic.
- Implement database reports and aggregation queries.
- Prepare report screenshots.

Main files:
- `src/main/java/edu/ucd/comp2013j/lab/dao/InventoryDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/InventoryService.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/ReportDao.java`
- `src/main/resources/web/index.html`
- `src/main/resources/web/app.js`

## Member E - Web Front-End, Testing, and Documentation

Responsibilities:
- Keep web front-end layout consistent and easy to demonstrate.
- Write HTML/CSS/JavaScript page logic, role-based navigation, registration dialog, and shared web helper functions.
- Keep interaction details usable, such as the fixed-slot schedule with clear available and booked states.
- Write tests and manual test cases.
- Combine documents and prepare the video script.

Main files:
- `src/main/resources/web/index.html`
- `src/main/resources/web/style.css`
- `src/main/resources/web/app.js`
- `src/main/java/edu/ucd/comp2013j/lab/model/*.java`
- `src/main/java/edu/ucd/comp2013j/lab/web/FormData.java`
- `src/main/java/edu/ucd/comp2013j/lab/web/Json.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/UserDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/AuthService.java`
- `src/test/java/edu/ucd/comp2013j/lab/SystemServiceTest.java`
- `docs/06-development-plan.md`
- `docs/07-test-document.md`
- `docs/09-report-outline.md`
- `docs/10-video-script.md`

## Collaboration Notes

- Each member should understand the full database design at a basic level.
- Each member should be able to run `mvn test` and `DB_PASSWORD='your_mysql_root_password' mvn exec:java`.
- The final report should clearly state what each person actually did. This document is a planning document and should be updated if the real work division changes.
