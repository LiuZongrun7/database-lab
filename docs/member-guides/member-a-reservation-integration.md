# Member A Guide - Project Integration and Reservation Module

## Your Claimed Area

Project integration, Maven setup, login/main frame integration, reservation workflow, and reservation consumable requests.

## Files You Should Understand

- `pom.xml`
- `README.md`
- `run.sh`
- `src/main/java/edu/ucd/comp2013j/lab/App.java`
- `src/main/java/edu/ucd/comp2013j/lab/web/LabWebServer.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/ReservationDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/ReservationService.java`
- `src/main/resources/web/app.js`

## What You Need To Understand

The most important part is the reservation transaction. In `ReservationService.requestReservation`, the system:

1. validates that the end time is after the start time;
2. starts a database transaction;
3. locks each selected equipment row with `FOR UPDATE`;
4. checks whether each equipment status allows booking;
5. checks whether another pending or approved reservation overlaps the requested time for any selected equipment item;
6. inserts a new reservation with `PENDING` status;
7. inserts rows into `reservation_equipment`;
8. inserts optional consumable needs into `reservation_consumables`;
9. commits if everything is valid, otherwise rolls back.

The overlap condition is:

`existing.start_time < new_end AND existing.end_time > new_start`

This catches partial overlap, full overlap, and surrounding overlap.

## Manual Things You Should Do

- Run `mvn test`.
- Run `DB_PASSWORD='your_mysql_root_password' mvn exec:java`.
- Create one valid reservation as `student1`.
- Select more than one equipment item in one reservation.
- Add one consumable request to the reservation.
- Try to create one conflicting reservation and confirm it is rejected.
- Login as `teacher` and approve a pending reservation.
- Take screenshots for the report.

## Possible Viva Questions

Q: Why did you use a transaction for reservation creation?

A: Because checking availability and inserting the reservation must happen as one unit. If two users reserve the same equipment at the same time, a transaction and row lock reduce the chance of inconsistent bookings.

Q: Why is the reservation first set to `PENDING`?

A: Some equipment can be high-risk or shared between courses, so a teacher or admin should approve it before use.

Q: Why is there a `reservation_equipment` table?

A: One reservation can include several equipment items. A link table is clearer than a single `equipment_id` column.

Q: Does a consumable request immediately reduce stock?

A: No. The reservation stores requested consumables. Real stock changes are still done through the inventory workflow by admin or technician.

Q: Why not use an ORM?

A: The assignment recommended avoiding ORM and complicated MVC frameworks. JDBC also makes the SQL and database design clearer for this coursework.

Q: How do you run the project?

A: From the project folder, run `mvn test` for tests and `DB_PASSWORD='your_mysql_root_password' mvn exec:java` to start the application.

Q: What are your limitations?

A: Passwords are plain text for demonstration, and the system does not send email notifications or support real check-in hardware.
