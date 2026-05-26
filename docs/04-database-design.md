# Database Design Document

## Overview

The database stores users, laboratories, student-lab membership, equipment, reservations, reservation consumable requests, approvals, maintenance tickets, maintenance updates, consumable stock, and stock transactions. It is designed to show common database concepts: entity relationships, many-to-many relationships, primary keys, foreign keys, check constraints, indexes, views, joins, aggregation, row locking, and transactions.

## ER Diagram

The report ER diagram is drawn as a conceptual Chen-style ER diagram. It follows
classic notation from the database lectures: rectangles for entity types, diamonds
for relationship types, ovals for attributes, underlined key attributes, single
lines for partial participation, double lines for total participation, and 1/N/M
labels for cardinality. In the rendered diagram, the cardinality labels are placed
near the relationship diamonds so that the entity rectangles remain readable.

The conceptual diagram does not draw every bridge table as a separate rectangle.
Instead, it shows the main business entities and relationships:

| Entity rectangle | Main attributes shown as ovals |
| --- | --- |
| `USER` | <u>user_id</u>, username, full_name, email, active, penalty_points |
| `ADMIN` | role subtype of `USER` |
| `STUDENT` | role subtype of `USER` |
| `TECHNICIAN` | role subtype of `USER` |
| `LAB` | <u>lab_id</u>, lab_code, lab_name, building, room, capacity |
| `EQUIPMENT` | <u>equipment_id</u>, asset_tag, equipment_name, category, status, risk_level |
| `RESERVATION` | <u>reservation_id</u>, start_time, end_time, purpose, status, created_at |
| `CONSUMABLE` | <u>consumable_id</u>, item_name, unit, quantity, reorder_level |
| `MAINTENANCE_TICKET` | <u>ticket_id</u>, title, priority, status, reported_at, resolved_at |
| `MAINTENANCE_UPDATE` | <u>update_id</u>, update_time, update_text |
| `STOCK_TRANSACTION` | <u>transaction_id</u>, change_amount, reason, created_at |

The `USER` entity is specialized into `ADMIN`, `STUDENT`, and `TECHNICIAN` using
a disjoint role circle. This matches the current role constraint: each account has
one role only.

Main relationship diamonds in the conceptual diagram:

| Relationship diamond | Meaning | Cardinality / participation idea |
| --- | --- | --- |
| `MANAGES` | admins manage labs | one admin can manage many labs; a lab may have a manager |
| `MEMBER_OF` | students belong to labs | M:N; used to enforce that students reserve only equipment from linked labs |
| `CONTAINS` | labs contain equipment | one lab contains many equipment items; every equipment item belongs to one lab |
| `BOOKS` | reservations book equipment | many reservations can refer to one equipment item; every reservation books one equipment item |
| `REQUESTS` | students create reservations | one student can make many reservations; every reservation has one requester |
| `APPROVES` | admins approve reservations | one admin can approve many reservations; approval decision details are attached to the relationship |
| `NEEDS` | reservations request consumables | M:N; `requested_quantity` is an attribute of this relationship |
| `STORES` | labs store consumables | one lab stores many consumables |
| `HAS_TICKET` | equipment has maintenance tickets | one equipment item can have many tickets |
| `REPORTS` | users report maintenance tickets | one user can report many tickets |
| `ASSIGNED_TO` | technicians handle maintenance tickets | one technician can handle many tickets; assignment can be empty before acceptance |
| `UPDATES_OF` | tickets have maintenance updates | one ticket can have many update records |
| `WRITES_UPDATE` | users write update records | one user can write many maintenance updates |
| `STOCK_CHANGE` | consumables have stock transactions | one consumable can have many stock changes |
| `ADJUSTS` | users perform stock changes | one user can perform many stock transactions |

The physical MySQL schema still contains 11 active tables. Two of them are bridge
tables for many-to-many relationships:

- `student_labs` implements the conceptual `STUDENT` M:N `LAB` relationship.
- `reservation_consumables` implements the conceptual `RESERVATION` M:N `CONSUMABLE`
  relationship and stores `requested_quantity`.

The three SQL views are not drawn as entity rectangles because they do not store
independent data. They are query results used for UI display and reports.

### Physical Schema Reference

The physical table-level design is summarized below. The full SQL definition is stored in `src/main/resources/db/schema.sql`.

## Table Summary

| Table | Purpose |
| --- | --- |
| `users` | Stores students, technicians, and administrators. |
| `labs` | Stores laboratory rooms and managers. |
| `student_labs` | Many-to-many relationship between students and labs. |
| `equipment` | Stores physical equipment and current status. |
| `reservations` | Stores single-equipment fixed-slot reservation requests and approved bookings. |
| `reservation_consumables` | Stores optional consumable needs submitted with a reservation. |
| `approvals` | Stores admin decisions for reservations. |
| `maintenance_tickets` | Stores equipment fault reports and repair workflow. |
| `maintenance_updates` | Stores ticket update history. |
| `consumables` | Stores consumable stock in each lab. |
| `stock_transactions` | Stores stock in/out transaction history. |

## Relational Mapping Notes

- One lab contains many pieces of equipment, so `equipment.lab_id` is a foreign key.
- A student can belong to many labs and a lab can contain many students, so `student_labs` resolves the many-to-many relationship.
- Each reservation is made by one user and references exactly one equipment item through `reservations.equipment_id`.
- A reservation can include optional consumable needs through `reservation_consumables`.
- A reservation can have approval records so the decision history is not lost.
- Maintenance tickets are linked to equipment and users. `technician_id` is nullable because a ticket can be open before assignment.
- Stock changes are not only stored as a current quantity. Each change is also inserted into `stock_transactions` for audit history.

## Constraints

Important constraints include:

- Unique usernames, emails, lab codes, and asset tags.
- Role check: `ADMIN`, `STUDENT`, or `TECHNICIAN`.
- Equipment status check: `AVAILABLE`, `RESERVED`, `IN_USE`, `MAINTENANCE`, or `RETIRED`.
- Reservation status check: `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`, `COMPLETED`, or `NO_SHOW`.
- Reservation time check: `end_time > start_time`.
- Stock quantity check: `quantity >= 0`.
- Foreign keys between all dependent tables.

## Indexes

- `idx_reservation_slot_lookup` supports conflict checking by equipment and time.
- `idx_reservation_time` supports time-range filtering for reservations.
- `idx_reservation_status` supports filtering pending and approved requests.
- `idx_ticket_status` supports maintenance workflow filtering.
- `idx_equipment_status` supports equipment availability queries.

## Views

`v_equipment_status` joins equipment and lab data and includes open ticket count. It is used by the equipment UI.

`v_user_reservation_history` joins reservations, users, equipment, and optional consumable request details. It is useful for report writing and manual checking.

`v_lab_usage_report` aggregates reservation counts per lab. It is used by the reports page.

## Transaction Design

Reservation creation is transactional:

1. Validate that the requested time matches one of the fixed slots: 08:00-14:00, 14:00-20:00, or 20:00-08:00.
2. Lock the selected equipment row using `FOR UPDATE`.
3. Check that the equipment status allows reservation.
4. If the requester is a student, check that `student_labs` links the student to the equipment's lab.
5. Check overlapping pending or approved reservations for that equipment item.
6. Insert the new pending reservation with `reservations.equipment_id`.
7. Insert optional rows into `reservation_consumables`.
8. Commit if all checks pass, otherwise roll back.

Inventory change is transactional:

1. Lock the consumable row using `FOR UPDATE`.
2. Calculate the new quantity.
3. Reject the operation if the result is negative.
4. Update quantity.
5. Insert stock transaction history.
6. Commit.

Maintenance report is transactional:

1. Insert a maintenance ticket.
2. Update equipment status to `MAINTENANCE`.
3. Commit both changes together.

Maintenance update also keeps equipment status consistent: if a ticket is changed to `RESOLVED`, the service checks whether the same equipment still has active tickets. The equipment is returned to `AVAILABLE` only when no active tickets remain.

## CREATE TABLE Statements

The full SQL schema is stored in `src/main/resources/db/schema.sql`. The sample data is stored in `src/main/resources/db/seed.sql`.
