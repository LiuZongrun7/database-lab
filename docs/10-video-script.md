# Five-Minute Video Script

Target length: around 5 minutes.

Each member should speak briefly. Replace member letters with real names.

## 0:00 - 0:30 Introduction - Member A

Hello, we are Group XX. Our project is a Campus Laboratory Equipment Reservation and Maintenance System. The problem we focus on is that university laboratories often share equipment between different student groups and labs. If this is managed by spreadsheets, it is easy to create time conflicts, forget maintenance status, or lose stock records. Our system uses a Web front-end with HTML, CSS, and JavaScript, a lightweight Java HTTP back-end, JDBC, and a MySQL database.

## 0:30 - 1:10 System Overview - Member A

The system has three main user roles: student, technician, and administrator. Students can register with one or more linked labs, search linked-lab equipment, request reservations, cancel their own requests, and report equipment problems. Technicians handle maintenance tickets and manage consumable inventory. Administrators can access all main functions, approve or reject reservations, manage equipment records, retire equipment, manage consumable item types, and view reports.

Show:
- login page;
- role-based workspace and main tabs.

## 1:10 - 1:55 Database Design - Member B

Our database contains 11 active tables and 3 views. It includes users, labs, student-lab membership, equipment, reservations, reservation consumable requests, approvals, maintenance tickets, maintenance updates, consumables, and stock transactions. The schema includes primary keys, foreign keys, unique constraints, check constraints, indexes, views, many-to-many relationships, and transaction logic.

Show:
- ER diagram from report;
- schema SQL briefly.

## 1:55 - 2:40 Reservation Demo - Member A

Now we demonstrate the reservation workflow. A student chooses one linked-lab equipment item, selects one green fixed time slot, optionally adds consumable needs, and submits a purpose. The service layer validates the fixed slot, locks the selected equipment row, checks its status, checks the student's `student_labs` access, and checks overlapping pending or approved reservations. If there is no conflict, the reservation is inserted with pending status and the selected equipment is stored in `reservations.equipment_id`; consumable requests are stored separately. Then an administrator can log in and approve or reject it. The approval decision is stored in the approvals table.

Show:
- login as student;
- create a reservation;
- login as admin;
- approve pending reservation.

## 2:40 - 3:20 Maintenance Demo - Member C

The maintenance module handles equipment faults. When a user reports a problem, the system creates a maintenance ticket and changes the equipment status to maintenance in the same transaction. This means the faulty equipment will no longer be available for new reservations. A technician or admin clicks accept to take an open ticket, and then clicks repaired when the fault is fixed. Equipment returns to available status only when all active tickets for that equipment are finished.

Show:
- Equipment tab;
- report problem;
- Maintenance tab;
- accept the ticket and mark it repaired.

## 3:20 - 4:00 Inventory and Reports - Member D

The inventory module manages consumable items, such as cables or sensor pads. Administrators can add new consumable item types, while administrators and technicians can adjust stock. When stock is changed, the system updates the current quantity and inserts a stock transaction record. It prevents the quantity from becoming negative. The reports page uses SQL views and aggregation queries to show lab usage and equipment status summaries.

Show:
- Inventory tab;
- valid stock change;
- failed negative stock attempt;
- Reports tab.

## 4:00 - 4:35 Testing - Member E

We tested the project using both automated and manual tests. The automated tests cover login, student registration, reservation conflict checking, inventory stock validation, stock update, consumable item creation, maintenance status changes, and the rule that equipment stays in maintenance until all active tickets are finished. Manual tests cover the main front-end workflows, including search, reservation approval, maintenance updates, inventory changes, role-based pages, and reports.

Show:
- terminal command `DB_PASSWORD='your_mysql_root_password' mvn test`;
- successful MySQL integration test result.

## 4:35 - 5:00 Conclusion - Member A

In conclusion, our system is a complete small information system with a database-backed Java application and a working web interface. The main database features are relational mapping, many-to-many relationships, constraints, views, indexes, transactions, row locking, and aggregation reports. In a future version, we could add hashed passwords, email notifications, QR code check-in, and equipment image upload.
