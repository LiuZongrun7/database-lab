# Member C Guide - Equipment and Maintenance Module

## Your Claimed Area

Equipment catalogue, admin equipment maintenance, problem reporting, maintenance ticket workflow, and equipment status changes.

## Files You Should Understand

- `src/main/java/edu/ucd/comp2013j/lab/dao/EquipmentDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/MaintenanceDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/MaintenanceService.java`
- `src/main/resources/web/index.html`
- `src/main/resources/web/app.js`
- `src/main/java/edu/ucd/comp2013j/lab/model/Equipment.java`
- `src/main/java/edu/ucd/comp2013j/lab/model/MaintenanceTicket.java`

## What You Need To Understand

The equipment page reads from the view `v_equipment_status`, so it can show lab information and open ticket count without writing a long join in the UI.

Admins can create, edit, and retire equipment. Retire means setting status to `RETIRED`, not deleting the row, because old reservations, tickets, and reports may still reference that equipment.

When a user reports a problem:

1. the UI collects title, description, and priority;
2. `MaintenanceService.reportProblem` starts a transaction;
3. it inserts a new row into `maintenance_tickets`;
4. it updates the equipment status to `MAINTENANCE`;
5. it commits both changes together.

When a technician accepts a ticket, the service assigns that user and changes the ticket to `IN_PROGRESS`. When the assigned technician or an admin marks it repaired, the ticket becomes `RESOLVED`. The equipment status returns to `AVAILABLE` only when all active tickets for that equipment are finished.

## Manual Things You Should Do

- Login as `student_net` and report a problem for available equipment.
- Login as `admin`, create a piece of equipment, edit it, and retire it.
- Check that equipment status becomes `MAINTENANCE`.
- Login as `tech`, accept the ticket, and then mark it repaired.
- Check that equipment status becomes `AVAILABLE` if there are no other active tickets for the same equipment.
- Prepare screenshots of both pages.

## Possible Viva Questions

Q: Why should maintenance ticket creation and equipment status update be in one transaction?

A: If the ticket is created but equipment status is not changed, users might still reserve broken equipment. If the status changes but the ticket is not created, technicians cannot track the repair. They should succeed or fail together.

Q: Why do you have `maintenance_updates`?

A: A ticket can have multiple progress updates. This table keeps the history instead of overwriting one note field.

Q: Why retire equipment instead of deleting it?

A: Deleting can break historical relationships with reservations, tickets, and reports. A `RETIRED` status keeps the history safe.

Q: What statuses can a ticket have?

A: `OPEN`, `IN_PROGRESS`, and `RESOLVED`.

Q: Why is `technician_id` nullable?

A: A ticket may be reported before a technician is assigned.

Q: How does maintenance affect reservation?

A: The reservation service checks equipment status. If it is `MAINTENANCE`, it rejects new reservation requests.
