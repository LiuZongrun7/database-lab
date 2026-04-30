# Test Document

## Test Environment

- Java: 17 or later
- Build tool: Maven
- Database: MySQL
- Front-end / UI: HTML, CSS, and JavaScript web pages

## Automated Tests

Run:

```bash
mvn test
```

Current automated tests are in:

`src/test/java/edu/ucd/comp2013j/lab/SystemServiceTest.java`

The automated tests connect to MySQL only when `DB_PASSWORD` is provided. Without `DB_PASSWORD`, Maven still compiles the project successfully, but the database integration tests are skipped to avoid storing a local password in the source code.

To run the full MySQL integration tests:

```bash
DB_PASSWORD='your_mysql_root_password' mvn test
```

## Automated Test Cases

| ID | Test | Expected Result |
| --- | --- | --- |
| AT-01 | Login with `admin/admin123` | Login succeeds. |
| AT-02 | Login with wrong password | Login fails. |
| AT-03 | Submit reservation overlapping an existing booking | System rejects it with a conflict message. |
| AT-04 | Decrease stock below zero | System rejects it. |
| AT-05 | Decrease stock by a valid amount | Quantity is updated by the correct amount. |
| AT-06 | Report maintenance problem | Equipment status changes to `MAINTENANCE`. |

## Manual Test Cases

### MT-01 Login

Steps:
1. Run `DB_PASSWORD='your_mysql_root_password' mvn exec:java`.
2. Enter `admin` and `admin123`.
3. Click Login.

Expected result:
- Main window opens.
- Header shows the logged-in user and role.

### MT-02 Search Equipment

Steps:
1. Open Equipment tab.
2. Type `robot` in the keyword field.
3. Click Search.

Expected result:
- The table shows the TurtleBot equipment row.

### MT-03 Create Reservation

Steps:
1. Login as `student1/student123`.
2. Open Reservations tab.
3. Click New Request.
4. Select available equipment.
5. Enter a future time such as `2026-05-08 10:00` to `2026-05-08 12:00`.
6. Enter a purpose and submit.

Expected result:
- A new reservation appears with status `PENDING`.

### MT-04 Approve Reservation

Steps:
1. Login as `teacher/teacher123`.
2. Open Reservations tab.
3. Select a pending reservation.
4. Click Approve.

Expected result:
- Reservation status changes to `APPROVED`.
- A record is inserted into `approvals`.

### MT-05 Reject Reservation

Steps:
1. Login as `teacher/teacher123`.
2. Select a pending reservation.
3. Click Reject.

Expected result:
- Reservation status changes to `REJECTED`.

### MT-06 Report Maintenance Problem

Steps:
1. Login as `student1/student123`.
2. Open Equipment tab.
3. Select equipment with `AVAILABLE` status.
4. Click Report Problem.
5. Fill title, description, and priority.

Expected result:
- A maintenance ticket is created.
- Equipment status changes to `MAINTENANCE`.

### MT-07 Update Maintenance Ticket

Steps:
1. Login as `tech/tech123`.
2. Open Maintenance tab.
3. Select an open ticket.
4. Click Update Ticket.
5. Select status `RESOLVED` and submit.

Expected result:
- Ticket status changes to `RESOLVED`.
- Equipment status changes back to `AVAILABLE`.

### MT-08 Change Stock

Steps:
1. Login as `tech/tech123`.
2. Open Inventory tab.
3. Select a consumable.
4. Click Change Stock.
5. Enter `-1` and reason `Used in lab`.

Expected result:
- Quantity decreases by one.
- A row is inserted into `stock_transactions`.

### MT-09 Prevent Negative Stock

Steps:
1. Open Inventory tab as technician.
2. Select a consumable.
3. Try to change stock by `-999`.

Expected result:
- System shows an error and keeps the old quantity.

### MT-10 Reports

Steps:
1. Login as admin.
2. Open Reports tab.

Expected result:
- Lab usage report and equipment status report are displayed.

## Remaining Limitations

- Password security is simplified for coursework.
- Front-end error messages are simple.
- The system does not send email notifications.
- The system does not include file uploads for equipment images.
