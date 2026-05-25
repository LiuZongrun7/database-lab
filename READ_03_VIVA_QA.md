# Viva Q&A For All Members

Each member should read this before the interview.

## Basic Questions

### What is this project?

It is a Campus Laboratory Equipment Reservation and Maintenance System. It manages equipment search, reservation requests, approval, maintenance tickets, consumable stock, and reports.

### Why is it an information system?

It stores and processes structured information about users, labs, equipment, reservations, approvals, maintenance, inventory, and reports. Users can create, update, search, and analyse this information.

### Why is it suitable for a database assignment?

It includes many related tables, many-to-many relationships, constraints, views, indexes, transaction logic, and aggregation reports.

### What is the front-end?

The front-end is a web interface written with HTML, CSS, and JavaScript. The Java program serves the web pages and JSON APIs through a lightweight HTTP server.

## Database Questions

### What are the main tables?

`users`, `labs`, `student_labs`, `equipment`, `reservations`, `reservation_consumables`, `approvals`, `maintenance_tickets`, `maintenance_updates`, `consumables`, and `stock_transactions`.

### Give one many-to-many relationship.

Students and labs are many-to-many, so we use `student_labs`. One student can belong to several labs, and one lab can contain many students. The reservation service uses this table to stop students from booking equipment outside their linked labs.

### Why do you have an approvals table?

The reservation table stores the current status. The approvals table stores decision history, including approver, decision time, decision, and comment.

### Why do you have stock_transactions?

The consumables table stores the current quantity. The stock_transactions table records every stock change, so we can know who changed stock, when, by how much, and why.

### What constraints did you use?

Primary keys, foreign keys, unique constraints, check constraints, and a reservation time check. For example, stock quantity cannot be negative, and reservation end time must be after start time.

## Transaction Questions

### How do you check reservation conflicts?

The overlap condition is:

`existing.start_time < new_end AND existing.end_time > new_start`

If that condition is true for the same equipment and an active reservation, the new request is rejected.

### Why use transactions?

Some operations update more than one table or depend on a check before insert. A transaction makes the operation succeed or fail as one unit.

### Give a transaction example.

For inventory, the system locks the consumable row, checks the current quantity, rejects negative stock, updates the quantity, inserts a stock transaction record, and commits.

## Implementation Questions

### Why use JDBC?

The assignment recommends avoiding ORM and complicated MVC frameworks. JDBC lets us show direct SQL and database operations clearly.

### Why use MySQL?

MySQL was used in the course labs, so it is easier for the team to explain in the interview. The system still uses JDBC directly, and the Java program automatically creates tables and inserts seed data when it starts.

### Why use a web front-end?

We have learned web development, so a web front-end is easier for the team to explain. It also matches the assignment's example of a user interface such as Web.

### What tests did you run?

We ran automated tests using `mvn test`. They cover login, reservation conflict rejection, negative stock prevention, valid stock update, and maintenance status update.

## Strong Short Answers

### Strongest database design point

The strongest point is that the system is not only CRUD. It has lab-based access control, reservation conflict checking, many-to-many relationships, approval history, maintenance workflow, stock transaction history, SQL views, and transactions.

### Main limitation

Passwords are plain text for coursework demonstration. A real system should hash passwords and add stronger permission management.

### Future improvement

We could add email notifications, QR code check-in, equipment image upload, hashed passwords, and a web front-end.
