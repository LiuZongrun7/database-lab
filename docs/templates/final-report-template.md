# Final Report Template

Replace all placeholder text before submission.

---

# COMP2013J Databases and Information Systems

## Campus Laboratory Equipment Reservation and Maintenance System

Group: `Group XX`

Members:

| Name | Student Number | Main Role |
| --- | --- | --- |
| Member A | 00000000 | Project integration and reservation module |
| Member B | 00000000 | Database design |
| Member C | 00000000 | Equipment and maintenance module |
| Member D | 00000000 | Inventory and reports module |
| Member E | 00000000 | Web front-end, testing, and documentation |

Submission Date: `1 June 2026`

---

## 1. System Description

### 1.1 Background

University laboratories often contain equipment that is shared by multiple courses, students, teachers, and technicians. If the equipment is managed manually using spreadsheets or messages, several problems can happen: two users may reserve the same equipment at the same time, faulty equipment may still be booked, maintenance records may be incomplete, and consumable stock changes may not be tracked.

Our project solves this problem by building a Java-based information system for campus laboratory equipment reservation and maintenance. The system supports equipment browsing, reservation requests, teacher approval, maintenance ticket handling, consumable stock management, and management reports.

### 1.2 System Users

The system has four main user roles:

| Role | Description |
| --- | --- |
| Student | Searches equipment, submits reservation requests, cancels own requests, reports equipment problems. |
| Teacher | Reviews reservation requests, approves or rejects bookings, views reports. |
| Technician | Updates maintenance tickets and manages consumable inventory. |
| Administrator | Has full access to reservations, maintenance, inventory, and reports. |

### 1.3 Main Functions

The main functions of the system are:

- login with seeded demo accounts;
- view and search laboratory equipment;
- submit equipment reservation requests;
- approve or reject pending reservation requests;
- cancel active personal reservations;
- report equipment faults;
- update maintenance ticket status;
- manage laboratory consumable stock;
- prevent invalid stock changes;
- view database reports.

### 1.4 Assumptions and Limitations

This project is designed as a database coursework system rather than a production system. Passwords are stored in plain text for easier demonstration, but a real system should use password hashing. The system does not send email notifications and does not include QR code check-in. The database uses MySQL because it matches the database system used in the course labs.

## 2. Requirements

### 2.1 Functional Requirements

| ID | Requirement |
| --- | --- |
| FR-01 | The system shall allow users to log in. |
| FR-02 | The system shall display equipment information, including lab, category, status, risk level, and open ticket count. |
| FR-03 | The system shall allow equipment search by keyword. |
| FR-04 | The system shall allow users to create reservation requests. |
| FR-05 | The system shall reject reservations with overlapping time for the same equipment. |
| FR-06 | The system shall reject reservations for equipment in maintenance or retired status. |
| FR-07 | The system shall allow teachers and administrators to approve or reject requests. |
| FR-08 | The system shall allow users to report equipment faults. |
| FR-09 | The system shall create maintenance tickets and update equipment status. |
| FR-10 | The system shall allow technicians to update ticket status. |
| FR-11 | The system shall allow stock changes for consumables. |
| FR-12 | The system shall prevent negative stock. |
| FR-13 | The system shall record stock transaction history. |
| FR-14 | The system shall provide report queries. |

### 2.2 Non-Functional Requirements

| ID | Requirement |
| --- | --- |
| NFR-01 | The system must be implemented in Java. |
| NFR-02 | The system must use a relational database. |
| NFR-03 | The system should use JDBC directly instead of ORM. |
| NFR-04 | The front-end/user interface should be simple and suitable for a short live demo. |
| NFR-05 | The source code should be easy for students to read and explain. |

## 3. System Design

### 3.1 Architecture

The project uses a simple layered architecture:

- Front-end layer: HTML, CSS, and JavaScript web pages.
- Web/API layer: lightweight Java HTTP server with JSON responses.
- Service layer: business rules and transactions.
- DAO layer: SQL statements and database access.
- Database layer: MySQL database with SQL schema and seed data.

This structure keeps SQL code separate from UI code and makes it easier to explain each part during the interview.

### 3.2 Package Structure

| Package | Purpose |
| --- | --- |
| `edu.ucd.comp2013j.lab.web` | Java HTTP server, form parsing, and JSON response helpers. |
| `src/main/resources/web` | HTML, CSS, and JavaScript web front-end files. |
| `edu.ucd.comp2013j.lab.service` | Business logic and transaction handling. |
| `edu.ucd.comp2013j.lab.dao` | Database access objects and SQL queries. |
| `edu.ucd.comp2013j.lab.model` | Java model classes. |
| `edu.ucd.comp2013j.lab.db` | Database connection and initialization. |

### 3.3 UML Diagrams

Insert diagrams from `docs/03-uml-modeling.md`:

- Use case diagram.
- Class diagram.
- Reservation sequence diagram.
- Maintenance sequence diagram.

## 4. Database Design

### 4.1 Database Overview

The database contains 12 tables:

| Table | Purpose |
| --- | --- |
| `users` | Stores students, teachers, technicians, and administrators. |
| `labs` | Stores laboratory rooms. |
| `equipment` | Stores equipment and current equipment status. |
| `courses` | Stores course information. |
| `course_members` | Maps users to courses. |
| `equipment_course_access` | Maps equipment to courses. |
| `reservations` | Stores reservation requests and booking status. |
| `approvals` | Stores approval or rejection decisions. |
| `maintenance_tickets` | Stores equipment fault reports. |
| `maintenance_updates` | Stores ticket progress history. |
| `consumables` | Stores current consumable stock. |
| `stock_transactions` | Stores stock change history. |

### 4.2 ER Diagram

Insert ER diagram from `docs/04-database-design.md`.

### 4.3 Important Relationships

The relationship between users and courses is many-to-many, so the table `course_members` is used. The relationship between equipment and courses is also many-to-many, so the table `equipment_course_access` is used.

Each reservation is linked to one user and one equipment item. A reservation can also be linked to a course. Each approval record is linked to one reservation and one approver. Each maintenance ticket is linked to one equipment item and one reporter, and it may also be linked to one technician.

### 4.4 Constraints

The schema includes several constraints:

- primary keys for all tables;
- foreign keys for relationships;
- unique constraints for username, email, lab code, asset tag, and course code;
- check constraints for role, equipment status, reservation status, priority, and non-negative quantity;
- reservation time check to make sure `end_time > start_time`.

### 4.5 Views

The project uses three views:

| View | Purpose |
| --- | --- |
| `v_equipment_status` | Shows equipment with lab data and open ticket count. |
| `v_user_reservation_history` | Shows reservation history with user and equipment details. |
| `v_lab_usage_report` | Aggregates reservation counts by lab. |

### 4.6 Transactions

Reservation creation uses a transaction. The system locks the selected equipment row, checks status, checks time conflict, inserts the reservation, and then commits. If any check fails, the transaction rolls back.

Inventory update also uses a transaction. The system locks the consumable row, calculates the new stock quantity, rejects the update if it would become negative, updates quantity, inserts stock transaction history, and commits.

Maintenance reporting uses a transaction to insert a maintenance ticket and change equipment status to `MAINTENANCE` together.

## 5. Implementation

### 5.1 Technology

| Technology | Reason |
| --- | --- |
| Java | Required by assignment and used for the back-end. |
| HTML/CSS/JavaScript | Web front-end learned by the group. |
| Java HTTP server | Lightweight server without a complex MVC framework. |
| JDBC | Direct SQL access and suitable for database coursework. |
| MySQL | Relational database system used in the course labs. |
| Maven | Dependency management and testing. |
| JUnit | Automated testing. |

### 5.2 Main Screens

Insert screenshots:

- Login screen.
- Equipment tab.
- Reservation tab.
- Maintenance tab.
- Inventory tab.
- Reports tab.

### 5.3 Module Explanation

The reservation module is implemented mainly in `ReservationService`, `ReservationDao`, and `ReservationPanel`. The service layer checks business rules and controls transactions, while the DAO layer contains SQL statements.

The maintenance module is implemented mainly in `MaintenanceService`, `MaintenanceDao`, `EquipmentDao`, `EquipmentPanel`, and `MaintenancePanel`. A reported equipment problem creates a ticket and changes equipment status.

The inventory module is implemented mainly in `InventoryService`, `InventoryDao`, and `InventoryPanel`. It records current stock and stock change history.

The report module is implemented in `ReportDao` and `ReportPanel`. It uses SQL views and aggregation queries.

## 6. Testing

### 6.1 Automated Testing

The automated tests are written in `SystemServiceTest.java`. They can be run using:

```bash
mvn test
```

Test result:

`Tests run: 5, Failures: 0, Errors: 0`

### 6.2 Automated Test Summary

| Test | Expected Result |
| --- | --- |
| Login with valid admin account | Login succeeds. |
| Login with wrong password | Login fails. |
| Overlapping reservation | Request is rejected. |
| Negative stock update | Request is rejected. |
| Valid stock update | Quantity changes correctly. |
| Maintenance report | Equipment status becomes maintenance. |

### 6.3 Manual Testing

Manual tests were performed for login, equipment search, reservation creation, approval, maintenance ticket update, inventory stock change, negative stock rejection, and report viewing. The full test plan is in `docs/07-test-document.md`.

## 7. Team Member Contribution

Replace this with actual contribution records.

| Member | Contribution |
| --- | --- |
| Member A | Project integration, reservation workflow, run instructions, demo coordination. |
| Member B | Database schema, ER diagram, seed data, database design documentation. |
| Member C | Equipment catalogue, maintenance workflow, maintenance demo preparation. |
| Member D | Inventory workflow, report queries, stock transaction design. |
| Member E | Web front-end code, model classes, testing, documentation organisation, video script. |

## 8. Self-Assessment Records

Attach edited Week 10 and Week 12 self-assessment records here.

## 9. AI Usage Statement

Example statement:

AI assistants were used during this project mainly to help draft documentation, organise report content, prepare diagram descriptions, and support parts of code generation and review. All AI-assisted materials were checked and edited by the team. The team members tested the system and prepared to explain the implemented database design, Java code, and system behaviour during the interview.

## 10. Conclusion

This project implemented a complete small information system for campus laboratory equipment management. It demonstrates relational database design, many-to-many relationships, constraints, views, indexes, transactions, and aggregation queries. The system also provides a Web front-end for practical demonstration, matching the assignment's example of building a web user interface. Future improvements could include password hashing, email notifications, QR code check-in, and equipment image upload.

## Appendices

- Appendix A: Full SQL schema.
- Appendix B: Seed data.
- Appendix C: Self-assessment records.
- Appendix D: Manual test cases.
- Appendix E: Screenshots.
