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
| AT-03 | Register a new student account | Account is created and can login. |
| AT-04 | Submit reservation overlapping an existing booking | System rejects it with a conflict message. |
| AT-05 | Decrease stock below zero | System rejects it. |
| AT-06 | Decrease stock by a valid amount | Quantity is updated by the correct amount. |
| AT-07 | Add a new consumable item type | New consumable row appears in inventory. |
| AT-08 | Report maintenance problem | Equipment status changes to `MAINTENANCE`. |

## Manual Test Cases

### MT-01 Login

Steps:
1. Run `DB_PASSWORD='your_mysql_root_password' mvn exec:java`.
2. Enter `admin` and `admin123`.
3. Click Login.

Expected result:
- Main window opens.
- Header shows the logged-in user and role.

### MT-02 Register Student

Steps:
1. Open the login page.
2. Click Register Student Account.
3. Fill username, full name, email, and password.
4. Submit the dialog.

Expected result:
- The new account is created with role `STUDENT`.
- The user enters the student workspace.

### MT-03 Search Equipment

Steps:
1. Open Equipment tab.
2. Type `robot` in the keyword field.
3. Click Search.

Expected result:
- The table shows the TurtleBot equipment row.

### MT-04 Create Reservation

Steps:
1. Login as `student1/student123`.
2. Open Reservations tab.
3. Open the equipment picker.
4. Select one or more available equipment items.
5. Optionally add one consumable request.
6. Enter a future time using the date-time controls.
7. Enter a purpose and submit.

Expected result:
- A new reservation appears with status `PENDING`.

### MT-05 Approve Reservation

Steps:
1. Login as `teacher/teacher123`.
2. Open Reservations tab.
3. Select a pending reservation.
4. Click Approve.

Expected result:
- Reservation status changes to `APPROVED`.
- A record is inserted into `approvals`.

### MT-06 Reject Reservation

Steps:
1. Login as `teacher/teacher123`.
2. Select a pending reservation.
3. Click Reject.

Expected result:
- Reservation status changes to `REJECTED`.

### MT-07 Report Maintenance Problem

Steps:
1. Login as `student1/student123`.
2. Open Equipment tab.
3. Select equipment with `AVAILABLE` status.
4. Click Report Problem.
5. Fill title, description, and priority.

Expected result:
- A maintenance ticket is created.
- Equipment status changes to `MAINTENANCE`.

### MT-08 Update Maintenance Ticket

Steps:
1. Login as `tech/tech123`.
2. Open Maintenance tab.
3. Select an open ticket.
4. Click Update Ticket.
5. Select status `RESOLVED` and submit.

Expected result:
- Ticket status changes to `RESOLVED`.
- Equipment status changes back to `AVAILABLE`.

### MT-09 Add Consumable Type

Steps:
1. Login as `admin/admin123`.
2. Open Inventory tab.
3. Click Add Consumable.
4. Fill lab, item name, unit, quantity, and reorder level.

Expected result:
- A new consumable item appears in the inventory table.

### MT-10 Change Stock

Steps:
1. Login as `tech/tech123`.
2. Open Inventory tab.
3. Select a consumable.
4. Click Change Stock.
5. Enter `-1` and reason `Used in lab`.

Expected result:
- Quantity decreases by one.
- A row is inserted into `stock_transactions`.

### MT-11 Prevent Negative Stock

Steps:
1. Open Inventory tab as technician.
2. Select a consumable.
3. Try to change stock by `-999`.

Expected result:
- System shows an error and keeps the old quantity.

### MT-12 Reports

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
