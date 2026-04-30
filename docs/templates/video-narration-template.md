# Video Narration Template

Target length: about 5 minutes. Replace member names and group number.

## Version A - Full Narration

### 0:00 - 0:25 Opening - Member A

Hello, we are Group XX. Our project is called Campus Laboratory Equipment Reservation and Maintenance System. It is a Java-based information system for managing shared laboratory equipment in a university. The system uses a Web front-end with HTML, CSS, and JavaScript, JDBC for database access, and an MySQL relational database.

### 0:25 - 0:55 Problem and Users - Member A

The problem we focus on is that laboratory equipment is often shared by different students, teachers, and courses. If reservations and maintenance are recorded manually, there may be time conflicts, broken equipment may still be booked, and stock changes may not be tracked. Our system has four roles: student, teacher, technician, and administrator.

### 0:55 - 1:25 Login and Main Web UI - Member E

Here is the login page. We use seeded demo accounts for different roles. After login, the main window contains five tabs: Equipment, Reservations, Maintenance, Inventory, and Reports. This is the front-end/user interface of our system. It is intentionally simple so the database functions are easy to demonstrate.

Show:
- login screen;
- admin login;
- main tabs.

### 1:25 - 2:05 Database Design - Member B

Our database contains 12 tables. The main tables include users, labs, equipment, courses, reservations, approvals, maintenance tickets, consumables, and stock transactions. We also use two junction tables for many-to-many relationships: course_members and equipment_course_access. The schema includes primary keys, foreign keys, unique constraints, check constraints, indexes, and views.

Show:
- ER diagram;
- `schema.sql`;
- maybe one table example.

### 2:05 - 2:55 Reservation Workflow - Member A

Now we demonstrate the reservation workflow. A student selects equipment, chooses a course, enters start time, end time, and purpose, then submits the request. The reservation is first stored as pending. The service layer checks whether the equipment can be reserved and whether there is an overlapping reservation. The overlap rule is existing start time before new end time, and existing end time after new start time.

Show:
- login as student;
- create pending reservation;
- try a conflicting reservation if time allows.

### 2:55 - 3:25 Approval Workflow - Member A or Member B

Next, a teacher or administrator can approve or reject the pending reservation. The current status is stored in the reservations table, while the decision history is stored in the approvals table. This means we can quickly see the current state but also keep an approval record.

Show:
- login as teacher/admin;
- approve pending reservation.

### 3:25 - 4:00 Maintenance Workflow - Member C

The maintenance module handles equipment faults. When a user reports a problem, the system creates a maintenance ticket and changes the equipment status to maintenance in the same transaction. This prevents broken equipment from being reserved. A technician can update the ticket and mark it as resolved, and then the equipment becomes available again.

Show:
- report problem;
- maintenance tab;
- update ticket to resolved.

### 4:00 - 4:30 Inventory and Reports - Member D

The inventory module manages consumable items in laboratories. When stock changes, the system updates the current quantity and inserts a stock transaction record. It also prevents the stock from becoming negative. The reports page uses SQL views and aggregation queries to show lab usage and equipment status summaries.

Show:
- inventory valid stock change;
- negative stock error;
- reports tab.

### 4:30 - 4:50 Testing - Member E

We tested the project using automated and manual tests. The automated tests cover login, reservation conflict checking, negative stock prevention, valid stock update, and maintenance status changes. The command `mvn test` runs all automated tests successfully.

Show:
- terminal `mvn test` success.

### 4:50 - 5:00 Closing - Member A

In conclusion, our system demonstrates database design, relationships, constraints, views, indexes, transactions, and Java JDBC implementation. Future improvements could include password hashing, email notifications, QR code check-in, and a web interface.

## Version B - Shorter Backup Script

Our project is a Campus Laboratory Equipment Reservation and Maintenance System. It is designed for university labs where equipment is shared by students, teachers, and technicians. The system supports login, equipment search, reservation requests, teacher approval, maintenance tickets, inventory management, and reports.

The project is implemented using a web front-end and a Java JDBC back-end. We use an MySQL database so the project can run easily on another machine. The database has 12 tables, including users, labs, equipment, courses, reservations, approvals, maintenance tickets, consumables, and stock transactions. It also includes many-to-many tables, constraints, views, indexes, and transactions.

The main workflow is reservation. A student creates a request with equipment, course, start time, end time, and purpose. The system checks equipment status and time conflicts. If the same equipment is already booked during an overlapping time, the request is rejected. A teacher or administrator can then approve or reject pending requests.

The maintenance workflow lets users report equipment problems. When a problem is reported, the system creates a maintenance ticket and changes equipment status to maintenance in one transaction. Technicians can update the ticket, and when it is resolved, the equipment can become available again.

The inventory workflow manages consumables. Stock changes are stored in both current quantity and transaction history. The system prevents negative stock. The reports page uses SQL views and aggregation queries to show lab usage and equipment status.

We tested the project with automated tests and manual front-end workflow tests. The tests cover login, reservation conflict, inventory validation, stock update, and maintenance status update.
