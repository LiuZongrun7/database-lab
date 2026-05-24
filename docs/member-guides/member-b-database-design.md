# Member B Guide - Database Design

## Your Claimed Area

Database schema, ER model, seed data, views, constraints, and relational mapping.

## Files You Should Understand

- `src/main/resources/db/schema.sql`
- `src/main/resources/db/seed.sql`
- `src/main/java/edu/ucd/comp2013j/lab/db/Database.java`
- `docs/03-uml-modeling.md`
- `docs/04-database-design.md`

## What You Need To Understand

The database has these main groups:

- User and permission data: `users`.
- Laboratory and equipment data: `labs`, `equipment`.
- Course access data: `courses`, `course_members`, `equipment_course_access`.
- Reservation data: `reservations`, `reservation_consumables`, `approvals`.
- Maintenance data: `maintenance_tickets`, `maintenance_updates`.
- Inventory data: `consumables`, `stock_transactions`.

Two many-to-many examples:

- A course has many users and a user can join many courses, so we use `course_members`.
- Equipment can be used by many courses and a course can access many equipment items, so we use `equipment_course_access`.
- Each reservation references one equipment item directly through `reservations.equipment_id`.
- A reservation can request several consumables with quantities, so we use `reservation_consumables`.

The schema uses:

- primary keys for every table;
- foreign keys for relationships;
- unique constraints for usernames, emails, asset tags, and course codes;
- check constraints for role, status, priority, and positive values;
- views for easier report queries;
- indexes for reservation and status queries.

## Manual Things You Should Do

- Open `schema.sql` and read each table.
- Compare the ER diagram with the SQL schema.
- Run the app and check that seeded users and equipment appear.
- Be ready to explain why `stock_transactions` is separate from `consumables`.
- Be ready to explain why approvals are a separate table instead of only a status column.
- Be ready to explain why reservation equipment and reservation consumables are separate link tables.

## Possible Viva Questions

Q: Why did you include both `reservations.status` and `approvals`?

A: The reservation status gives the current state quickly. The approvals table stores the decision history, including approver, decision time, and comment.

Q: What is the strongest database design part of this project?

A: The design is not just separate tables. It includes many-to-many relationships, constraints, views, indexes, and transaction-based workflows for reservation and inventory.

Q: Why do you have `stock_transactions`?

A: Current quantity alone is not enough. The transaction table records who changed stock, when it changed, by how much, and why.

Q: Why not store consumable requests as text inside `reservations`?

A: A reservation can request multiple consumables, each with a quantity. A separate table keeps the design normalized and queryable.

Q: What views did you create?

A: `v_equipment_status`, `v_user_reservation_history`, and `v_lab_usage_report`.

Q: What would you change in a real system?

A: I would add password hashing, more detailed permission tables, audit logs for all status changes, and possibly migrate to PostgreSQL or MySQL.
