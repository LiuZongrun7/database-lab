# Five-Minute Video Script

Target length: around 5 minutes.

Each member should speak briefly. Replace member letters with real names.

## 0:00 - 0:30 Introduction - Member A

Hello, we are Group XX. Our project is a Campus Laboratory Equipment Reservation and Maintenance System. The problem we focus on is that university laboratories often share equipment between different courses and users. If this is managed by spreadsheets, it is easy to create time conflicts, forget maintenance status, or lose stock records. Our system uses a Web front-end with HTML, CSS, and JavaScript, a Java JDBC back-end, and an MySQL database.

## 0:30 - 1:10 System Overview - Member A

The system has four main user roles: student, teacher, technician, and administrator. Students can search equipment, request reservations, cancel their own requests, and report equipment problems. Teachers can approve or reject reservations. Technicians can update maintenance tickets and manage consumable inventory. Administrators can access all main functions and reports.

Show:
- login page;
- main window tabs.

## 1:10 - 1:55 Database Design - Member B

Our database includes users, labs, equipment, courses, reservations, approvals, maintenance tickets, maintenance updates, consumables, and stock transactions. We also use two many-to-many tables: course members and equipment-course access. The schema includes primary keys, foreign keys, unique constraints, check constraints, indexes, and views. For example, reservations reference users and equipment, and maintenance tickets reference both equipment and reporter.

Show:
- ER diagram from report;
- schema SQL briefly.

## 1:55 - 2:40 Reservation Demo - Member A

Now we demonstrate the reservation workflow. A student can choose equipment, select a course, enter a start and end time, and submit a purpose. The service layer checks equipment status and overlapping time. If there is no conflict, the reservation is inserted with pending status. Then a teacher can log in and approve or reject it. The approval decision is stored in the approvals table.

Show:
- login as student;
- create a reservation;
- login as teacher/admin;
- approve pending reservation.

## 2:40 - 3:20 Maintenance Demo - Member C

The maintenance module handles equipment faults. When a user reports a problem, the system creates a maintenance ticket and changes the equipment status to maintenance in the same transaction. This means the faulty equipment will no longer be available for new reservations. A technician can later assign the ticket, update the status, and mark it as resolved.

Show:
- Equipment tab;
- report problem;
- Maintenance tab;
- update ticket to resolved.

## 3:20 - 4:00 Inventory and Reports - Member D

The inventory module manages consumable items, such as cables or sensor pads. When stock is changed, the system updates the current quantity and inserts a stock transaction record. It prevents the quantity from becoming negative. The reports page uses SQL views and aggregation queries to show lab usage and equipment status summaries.

Show:
- Inventory tab;
- valid stock change;
- failed negative stock attempt;
- Reports tab.

## 4:00 - 4:35 Testing - Member E

We tested the project using both automated and manual tests. The automated tests cover login, reservation conflict checking, inventory stock validation, stock update, and maintenance status changes. Manual tests cover the main front-end workflows, including search, reservation approval, maintenance updates, inventory changes, and reports.

Show:
- terminal command `mvn test`;
- successful test result.

## 4:35 - 5:00 Conclusion - Member A

In conclusion, our system is a complete small information system with a database-backed Java application. The main database features are relational mapping, many-to-many relationships, constraints, views, indexes, transactions, and aggregation reports. In a future version, we could add hashed passwords, email notifications, QR code check-in, and a web interface.
