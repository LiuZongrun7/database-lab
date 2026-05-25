# Project Overview For Team Members

## Project Name

Campus Laboratory Equipment Reservation and Maintenance System

## What The System Does

This is a Java database information system for managing shared university laboratory equipment.

It supports:

- user login;
- equipment search;
- equipment reservation requests;
- administrator approval or rejection;
- maintenance ticket reporting;
- technician ticket updates;
- consumable inventory management;
- stock transaction history;
- management reports.

## Why This Topic Works For The Assignment

The assignment asks for an information system with a database complex enough to cover lecture and lab knowledge. This topic is suitable because it naturally includes:

- multiple user roles;
- many database entities;
- one-to-many and many-to-many relationships;
- reservation time conflict checking;
- equipment maintenance status;
- stock management;
- transaction history;
- SQL views and reports;
- constraints and indexes.

## Technology

| Part | Choice |
| --- | --- |
| Programming language | Java |
| Front-end / user interface | HTML, CSS, and JavaScript web pages |
| Database access | JDBC |
| Database | MySQL |
| Build tool | Maven |
| Tests | JUnit |

The PDF says the system can include a nice user interface, for example Web or application. This project uses the Web option: a browser-based front-end served by a lightweight Java HTTP server.

## Run Commands

From the project root:

```bash
DB_PASSWORD='your_mysql_root_password' mvn exec:java
```

Or:

```bash
DB_PASSWORD='your_mysql_root_password' ./run.sh
```

Then open:

```text
http://localhost:8080
```

## Demo Accounts

| Username | Password | Role |
| --- | --- | --- |
| admin | 123 | ADMIN |
| tech | 123 | TECHNICIAN |
| student_ai | 123 | STUDENT |
| student_bio | 123 | STUDENT |
| student_net | 123 | STUDENT |
| student_multi | 123 | STUDENT |

## Main Demo Flow

1. Login as `student_net`.
2. Search equipment in the Equipment tab.
3. Create a reservation request in the Reservations tab.
4. Login as `admin`.
5. Approve or reject the pending reservation.
6. Login as `student_net` or `admin`.
7. Report an equipment problem.
8. Login as `tech`.
9. Update the maintenance ticket to resolved.
10. Change inventory stock and show reports.

## Most Important Database Points To Mention

- `student_labs` is the many-to-many table that links students to labs.
- Students can only reserve equipment from their linked labs.
- `reservations` uses time conflict checking.
- `approvals` stores approval history.
- `maintenance_tickets` and `maintenance_updates` store repair workflow and history.
- `stock_transactions` stores inventory audit history.
- Views are used for equipment status and reports.
- Transactions are used for reservation creation, maintenance reporting, and inventory updates.
