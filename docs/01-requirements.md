# Project Requirements Document

## Project Name

Campus Laboratory Equipment Reservation and Maintenance System

## Background

University laboratories often share expensive equipment between modules, research groups, technicians, and students. Simple spreadsheets are easy to lose control of because they do not check time conflicts, maintenance status, user permissions, or consumable stock. This project builds a small but complete information system for managing laboratory equipment reservations and maintenance work.

## Users

- Student: views linked-lab equipment, submits reservation requests, reports equipment problems, views own reservations.
- Technician: handles maintenance tickets, updates repair status, manages consumable stock.
- Administrator: has full access to equipment, reservations, maintenance, inventory, and reports.

## Functional Requirements

1. The system shall allow users to log in using a username and password.
2. The system shall display equipment with lab, category, risk level, status, and open ticket count.
3. The system shall allow users to search equipment by asset tag, name, or category.
4. The system shall allow students to create reservation requests with equipment, fixed time slot, and purpose.
5. The system shall reject reservation requests when the equipment is already booked for an overlapping time period.
6. The system shall reject reservation requests when equipment is in maintenance or retired status.
7. The system shall reject student reservation requests for equipment outside the student's linked labs.
8. The system shall allow administrators to approve or reject pending reservations.
9. The system shall allow users to cancel their own active reservations.
10. The system shall allow users to report equipment faults.
11. The system shall automatically move faulty equipment to maintenance status when a maintenance ticket is created.
12. The system shall allow technicians and administrators to accept maintenance tickets and mark repairs finished.
13. The system shall return equipment to available status when all active tickets are resolved.
14. The system shall show consumable stock for each laboratory.
15. The system shall allow technicians and administrators to increase or decrease consumable stock.
16. The system shall prevent stock quantity from becoming negative.
17. The system shall record every stock change in a stock transaction table.
18. The system shall provide simple management reports for lab usage and equipment status.

## Non-Functional Requirements

- The system must be implemented in Java.
- The system must use JDBC and SQL directly. ORM frameworks are not used.
- The database design should include primary keys, foreign keys, check constraints, indexes, views, and many-to-many relationships.
- The code should be easy for students to read and explain.
- The system should run on a normal laptop with Maven and Java installed.
- The front-end/user interface should be simple enough for a short live demonstration. This project uses a Web UI with HTML, CSS, and JavaScript.

## Assumptions

- Passwords are stored as plain text only for coursework demonstration. A real system should hash passwords.
- Payment, QR code check-in, and email notifications are outside the scope of this version.
- MySQL is used as the relational database system because it matches the course lab environment.
- All seed data is sample data and does not represent real students or staff.

## Main Success Criteria

- A student can submit a reservation request.
- The request is limited to equipment in the student's linked labs.
- An administrator can approve that request.
- A conflicting request is blocked by the database service logic.
- A reported fault creates a maintenance ticket and changes equipment status.
- A technician can update the maintenance ticket.
- Inventory changes are transactional and cannot create negative stock.
- Reports show useful database aggregation results.
