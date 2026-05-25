# Database Design Document

## Overview

The database stores users, laboratories, student-lab membership, equipment, reservations, reservation consumable requests, approvals, maintenance tickets, maintenance updates, consumable stock, and stock transactions. It is designed to show common database concepts: entity relationships, many-to-many relationships, primary keys, foreign keys, check constraints, indexes, views, joins, aggregation, row locking, and transactions.

## ER Diagram

Rendered Chen-style conceptual ER diagram for the report. This diagram follows the notation taught in Lecture 8 and Lecture 9: rectangles for entity types, diamonds for relationships, ovals for attributes, underlined key attributes, double lines for total participation, a double rectangle for a weak/dependent entity, a double diamond for its identifying relationship, a dashed underline for a partial key, composite attributes, derived attributes, and a disjoint subtype circle. The diagram keeps the main business entities and relationships readable; the physical schema diagram below shows the full table-level implementation.

- `docs/chen-er-diagram-current.svg`
- `docs/chen-er-diagram-current.png`

Notation choices in the rendered diagram:

- `RESERVATION` has total participation in `REQUESTS` because every reservation has a requester.
- `RESERVATION` has total participation in `INCLUDES` because the current service requires exactly one equipment item for each reservation.
- `EQUIPMENT`, `CONSUMABLE`, `MAINTENANCE_TICKET`, and `MAINTENANCE_UPDATE` use double lines where the current schema or service requires the owner relationship.
- `MAINTENANCE_UPDATE` is shown as a weak/dependent entity because update notes are meaningful only under one maintenance ticket; `UPDATES_OF` is therefore drawn as the identifying relationship.
- `time_period` and `location` are composite attributes. `open_ticket_count` and `reservation_count` are derived attributes produced by report/view queries rather than simple stored fields.
- `USER` is specialized into disjoint role subtypes (`ADMIN`, `STUDENT`, `TECHNICIAN`) to match the `role` constraint in the current schema.

Physical schema reference diagram:

- `docs/er-diagram-current.svg`
- `docs/er-diagram-current-full.png`

```mermaid
erDiagram
    USERS ||--o{ LABS : manages
    USERS ||--o{ STUDENT_LABS : belongs_to
    LABS ||--o{ STUDENT_LABS : has_students
    LABS ||--o{ EQUIPMENT : contains
    USERS ||--o{ RESERVATIONS : requests
    EQUIPMENT ||--o{ RESERVATIONS : booked_for
    RESERVATIONS ||--o{ RESERVATION_CONSUMABLES : requests
    CONSUMABLES ||--o{ RESERVATION_CONSUMABLES : requested_in
    RESERVATIONS ||--o{ APPROVALS : receives
    USERS ||--o{ APPROVALS : decides
    EQUIPMENT ||--o{ MAINTENANCE_TICKETS : has
    USERS ||--o{ MAINTENANCE_TICKETS : reports
    USERS ||--o{ MAINTENANCE_TICKETS : assigned_to
    MAINTENANCE_TICKETS ||--o{ MAINTENANCE_UPDATES : contains
    USERS ||--o{ MAINTENANCE_UPDATES : writes
    LABS ||--o{ CONSUMABLES : stores
    CONSUMABLES ||--o{ STOCK_TRANSACTIONS : records
    USERS ||--o{ STOCK_TRANSACTIONS : performs

    USERS {
      int user_id PK
      varchar username UK
      varchar password
      varchar full_name
      varchar email UK
      varchar role
      int penalty_points
      boolean active
    }

    LABS {
      int lab_id PK
      varchar lab_code UK
      varchar lab_name
      varchar building
      varchar room
      int capacity
      int manager_id FK
    }

    EQUIPMENT {
      int equipment_id PK
      varchar asset_tag UK
      varchar equipment_name
      varchar category
      int lab_id FK
      varchar status
      date purchase_date
      varchar risk_level
      varchar notes
    }

    STUDENT_LABS {
      int user_id PK FK
      int lab_id PK FK
    }

    RESERVATIONS {
      int reservation_id PK
      int requester_id FK
      int equipment_id FK
      timestamp start_time
      timestamp end_time
      varchar purpose
      varchar status
      timestamp created_at
    }

    RESERVATION_CONSUMABLES {
      int reservation_id PK FK
      int consumable_id PK FK
      int requested_quantity
    }

    APPROVALS {
      int approval_id PK
      int reservation_id FK
      int approver_id FK
      varchar decision
      timestamp decision_time
      varchar comment
    }

    MAINTENANCE_TICKETS {
      int ticket_id PK
      int equipment_id FK
      int reporter_id FK
      int technician_id FK
      varchar title
      varchar description
      varchar priority
      varchar status
      timestamp reported_at
      timestamp resolved_at
    }

    MAINTENANCE_UPDATES {
      int update_id PK
      int ticket_id FK
      int user_id FK
      timestamp update_time
      varchar update_text
    }

    CONSUMABLES {
      int consumable_id PK
      int lab_id FK
      varchar item_name
      varchar unit
      int quantity
      int reorder_level
    }

    STOCK_TRANSACTIONS {
      int transaction_id PK
      int consumable_id FK
      int user_id FK
      int change_amount
      varchar reason
      timestamp created_at
    }
```

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
