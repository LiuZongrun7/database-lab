# Viva / Interview Q&A Bank

These are likely questions and example answers. Team members should answer in their own words.

## General Project

### Q1. What is your project about?

Our project is a campus laboratory equipment reservation and maintenance system. It helps users search equipment, request reservations, approve bookings, report equipment problems, manage consumable stock, and view management reports.

### Q2. Why did you choose this topic?

We chose it because it is less generic than a normal library or shopping system, but it still has strong database requirements. It naturally includes reservations, time conflicts, maintenance history, inventory, many-to-many relationships, transactions, and reports.

### Q3. What are the main roles?

The roles are student, teacher, technician, and administrator. Students request reservations and report problems. Teachers approve or reject reservations. Technicians update maintenance tickets and manage stock. Administrators can access all main functions.

### Q4. What is the most important feature?

The most important feature is reservation management with conflict checking. The system prevents two active reservations for the same equipment from overlapping.

## Database Design

### Q5. How many tables are in your database?

There are 12 main tables: users, labs, equipment, courses, course_members, equipment_course_access, reservations, approvals, maintenance_tickets, maintenance_updates, consumables, and stock_transactions.

### Q6. Give an example of a many-to-many relationship.

Users and courses are many-to-many because one user can join several courses and one course can have many users. We use `course_members` as the junction table. Equipment and courses are also many-to-many, using `equipment_course_access`.

### Q7. Why do you have an approvals table?

The reservation table stores the current status, but the approvals table stores decision history, including who approved or rejected the request, when the decision happened, and the comment.

### Q8. Why do you have stock_transactions?

The consumables table only stores current stock. The stock_transactions table records every stock change, including user, amount, reason, and time. This gives us an audit history.

### Q9. What constraints did you use?

We used primary keys, foreign keys, unique constraints, check constraints, and a reservation time check. For example, equipment status must be one of the allowed values, and stock quantity cannot be negative.

### Q10. What views did you use?

We created `v_equipment_status`, `v_user_reservation_history`, and `v_lab_usage_report`. The first one is used by the equipment page, and the lab usage view supports the report page.

## Reservation Logic

### Q11. How do you detect reservation time conflicts?

We check whether an existing active reservation starts before the new end time and ends after the new start time:

`existing.start_time < new_end AND existing.end_time > new_start`

This catches all overlapping cases.

### Q12. Why use a transaction for reservation creation?

The availability check and insertion must be treated as one unit. If one step succeeds and another fails, the database could become inconsistent. The transaction allows us to roll back on error.

### Q13. Why use `FOR UPDATE`?

It locks the selected row during the transaction. In this project, we use it when checking equipment status and stock quantity to reduce inconsistent updates.

### Q14. Can a user reserve equipment in maintenance?

No. The reservation service checks equipment status. If the status is `MAINTENANCE` or `RETIRED`, it rejects the request.

## Maintenance

### Q15. What happens when a user reports a problem?

The system creates a maintenance ticket and changes the equipment status to `MAINTENANCE` in the same transaction.

### Q16. Why should those two actions be in the same transaction?

If the ticket is created but equipment status is not changed, broken equipment could still be reserved. If status changes but the ticket is not created, technicians have no repair record.

### Q17. How does equipment become available again?

When a technician or admin updates the ticket to `RESOLVED` or `CLOSED`, the service updates the related equipment status back to `AVAILABLE`.

## Inventory

### Q18. How do you prevent negative stock?

The service locks the consumable row, reads the current quantity, calculates the new quantity, and rejects the operation if it would be below zero. The database table also has a check constraint.

### Q19. Why is inventory included in a lab equipment system?

Some equipment needs consumables, such as sensor pads, batteries, cables, and SD cards. Tracking them makes the system more realistic and gives the database design more transaction and audit history.

## Java / Implementation

### Q20. Why did you use JDBC instead of ORM?

The assignment said ORM and complicated MVC frameworks are not recommended. JDBC also lets us show SQL directly, which is better for a database course.

### Q21. Why use a web front-end?

Our group has learned web development, so HTML, CSS, and JavaScript are easier for us to understand and explain. The assignment also lists Web as an example user interface.

### Q22. What is the difference between DAO and service classes?

DAO classes contain SQL and database access code. Service classes contain business rules and transactions, such as conflict checking and negative stock prevention.

### Q23. What automated tests did you write?

We tested valid and invalid login, reservation conflict rejection, negative stock rejection, valid stock update, and maintenance status update.

## Limitations and Improvements

### Q24. What are the limitations?

The system uses plain text passwords for coursework demonstration. It does not send emails, does not support QR check-in, and does not upload equipment images.

### Q25. What would you improve in the future?

We would add password hashing, email notifications, QR code check-in, equipment image upload, a web interface, and a more flexible permission table.

## Member-Specific Quick Answers

### Member A

I focused on project integration and reservation. I can explain how a reservation request is created, how overlap is checked, and how approval changes the reservation status.

### Member B

I focused on database design. I can explain the ER model, relational mapping, many-to-many tables, constraints, views, and indexes.

### Member C

I focused on equipment and maintenance. I can explain how reporting a problem creates a ticket and changes equipment status.

### Member D

I focused on inventory and reports. I can explain stock transactions, negative stock prevention, and report queries.

### Member E

I focused on web front-end, testing, and documentation. I can explain how the browser calls Java API endpoints and what tests were used.
