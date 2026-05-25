# Video Narration Template

Target length: about 5 minutes. Replace member names and group number.

## Version A - Full Narration

### 0:00 - 0:25 Opening - Member A

Hello, we are Group XX. Our project is called Campus Laboratory Equipment Reservation and Maintenance System. It is a Java-based information system for managing shared laboratory equipment in a university. The system uses a Web front-end with HTML, CSS, and JavaScript, a lightweight Java HTTP server, JDBC for database access, and a MySQL relational database.

### 0:25 - 0:55 Problem and Users - Member A

The problem we focus on is that laboratory equipment is often shared by different student groups and labs. If reservations and maintenance are recorded manually, there may be time conflicts, broken equipment may still be booked, and stock changes may not be tracked. Our system has three roles: student, technician, and administrator.

### 0:55 - 1:25 Login and Main Web UI - Member E

Here is the login page. We use seeded demo accounts for different roles. After login, the main window contains five tabs: Equipment, Reservations, Maintenance, Inventory, and Reports. This is the front-end/user interface of our system. It is intentionally simple so the database functions are easy to demonstrate.

Show:
- login screen;
- admin login;
- main tabs.

### 1:25 - 2:05 Database Design - Member B

Our database contains 11 active tables and 3 views. The main tables include users, labs, student_labs, equipment, reservations, reservation consumable requests, approvals, maintenance tickets, maintenance updates, consumables, and stock transactions. We also use junction tables for many-to-many relationships, including student_labs and reservation_consumables. The schema includes primary keys, foreign keys, unique constraints, check constraints, indexes, and views.

Show:
- ER diagram;
- `schema.sql`;
- maybe one table example.

### 2:05 - 2:55 Reservation Workflow - Member A

Now we demonstrate the reservation workflow. A student selects one linked-lab equipment item, chooses a green fixed slot, enters the purpose and optional consumable needs, then submits the request. The reservation is first stored as pending. The service layer checks whether the selected equipment can be reserved, whether the student is linked to that lab, and whether there is an overlapping reservation. The overlap rule is existing start time before new end time, and existing end time after new start time.

Show:
- login as student;
- create pending reservation;
- try a conflicting reservation if time allows.

### 2:55 - 3:25 Approval Workflow - Member A or Member B

Next, an administrator can approve or reject the pending reservation. The current status is stored in the reservations table, while the decision history is stored in the approvals table. This means we can quickly see the current state but also keep an approval record.

Show:
- login as admin;
- approve pending reservation.

### 3:25 - 4:00 Maintenance Workflow - Member C

The maintenance module handles equipment faults. When a user reports a problem, the system creates a maintenance ticket and changes the equipment status to maintenance in the same transaction. This prevents broken equipment from being reserved. A technician or admin clicks accept to take an open ticket, and then clicks repaired when the fault is fixed. The equipment becomes available again only when all active tickets for that equipment are finished.

Show:
- report problem;
- maintenance tab;
- accept the ticket and mark it repaired.

### 4:00 - 4:30 Inventory and Reports - Member D

The inventory module manages consumable items in laboratories. When stock changes, the system updates the current quantity and inserts a stock transaction record. It also prevents the stock from becoming negative. The reports page uses SQL views and aggregation queries to show lab usage and equipment status summaries.

Show:
- inventory valid stock change;
- negative stock error;
- reports tab.

### 4:30 - 4:50 Testing - Member E

We tested the project using automated and manual tests. The automated tests cover login, student registration, reservation conflict checking, negative stock prevention, valid stock update, consumable type creation, maintenance status changes, and the rule that equipment stays in maintenance until all active tickets are finished. The command `DB_PASSWORD='your_mysql_root_password' mvn test` runs the full MySQL integration tests.

Show:
- terminal `DB_PASSWORD='your_mysql_root_password' mvn test` success.

### 4:50 - 5:00 Closing - Member A

In conclusion, our system demonstrates database design, relationships, constraints, views, indexes, transactions, row locking, Java JDBC implementation, and a working web interface. Future improvements could include password hashing, email notifications, QR code check-in, and equipment image upload.

## Version B - Shorter Backup Script

Our project is a Campus Laboratory Equipment Reservation and Maintenance System. It is designed for university labs where equipment is shared by students and technicians. The system supports login, equipment search, lab-based reservation requests, admin approval, maintenance tickets, inventory management, and reports.

The project is implemented using a web front-end and a Java JDBC back-end. We use a MySQL database so the project can run easily on another machine. The database has 11 active tables, including users, labs, student_labs, equipment, reservations, reservation consumable requests, approvals, maintenance tickets, consumables, and stock transactions. It also includes many-to-many tables, constraints, views, indexes, and transactions.

The main workflow is reservation. A student creates a request with one linked-lab equipment item, a fixed slot, purpose, and optional consumable needs. The system checks equipment status, student-lab access, and time conflicts. If the selected equipment item is already booked during the slot, the request is rejected. An administrator can then approve or reject pending requests.

The maintenance workflow lets users report equipment problems. When a problem is reported, the system creates a maintenance ticket and changes equipment status to maintenance in one transaction. Technicians handle tickets through accept and repaired buttons, and equipment can become available again when all active tickets for that equipment are resolved.

The inventory workflow manages consumables. Stock changes are stored in both current quantity and transaction history. The system prevents negative stock. The reports page uses SQL views and aggregation queries to show lab usage and equipment status.

We tested the project with automated tests and manual front-end workflow tests. The tests cover login, student registration, reservation conflict, inventory validation, stock update, consumable creation, and maintenance status update.
