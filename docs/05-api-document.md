# Web Interface and API Document

This project uses a web front-end written with HTML, CSS, and JavaScript. The Java back-end starts a lightweight HTTP server and provides API endpoints. The browser calls these endpoints using JavaScript `fetch()`. API handlers call service classes, and service classes use DAO classes to access the database.

## Static Web Pages

| File | Purpose |
| --- | --- |
| `src/main/resources/web/index.html` | Main web page with login, tabs, forms, tables, and dialog. |
| `src/main/resources/web/style.css` | Front-end styling and responsive layout. |
| `src/main/resources/web/app.js` | Browser logic and API calls. |

## API Endpoints

| Endpoint | Method | Purpose |
| --- | --- | --- |
| `/api/login` | POST | Login and return user info. |
| `/api/register` | POST | Register a new student account. |
| `/api/labs` | GET | List labs for admin and registration forms. |
| `/api/equipment` | GET | List or search equipment visible to the current user. |
| `/api/equipment/save` | POST | Admin creates or updates equipment. |
| `/api/equipment/retire` | POST | Admin marks equipment as retired. |
| `/api/users/technicians` | GET | List technicians for maintenance assignment. |
| `/api/reservations` | GET | List reservations visible to current user. |
| `/api/reservations/slots` | GET | List fixed-slot availability for one equipment item. |
| `/api/reservations/create` | POST | Create a reservation request. |
| `/api/reservations/decide` | POST | Approve or reject a reservation. |
| `/api/reservations/cancel` | POST | Cancel user's own active reservation. |
| `/api/maintenance` | GET | List maintenance tickets. |
| `/api/maintenance/report` | POST | Report equipment problem. |
| `/api/maintenance/update` | POST | Accept a maintenance ticket or mark it repaired. |
| `/api/inventory` | GET | List consumable stock visible to the current user. |
| `/api/inventory/add` | POST | Admin creates a new consumable item type. |
| `/api/inventory/change` | POST | Change stock amount. |
| `/api/reports/lab-usage` | GET | Lab usage report. |
| `/api/reports/equipment-status` | GET | Equipment status report. |

## Authentication

### `AuthService.login(String username, String password)`

Purpose: check login details and return a user if valid.

Input:
- `username`
- `password`

Output:
- `Optional<User>`

Related SQL:
- `SELECT ... FROM users WHERE username = ? AND password = ? AND active = TRUE`

### `AuthService.registerStudent(...)`

Purpose: create a self-service student account.

Input:
- `username`
- `password`
- `fullName`
- `email`
- `labIds`

Output:
- new `User`

Rule:
- self-registration always creates role `STUDENT`.
- at least one lab id is required and stored in `student_labs`.

## Equipment

### `EquipmentDao.findVisibleFor(User user)`

Purpose: return equipment with lab and open ticket count. Students only see equipment from labs linked in `student_labs`.

Output:
- `List<Equipment>`

Related SQL:
- Reads from `v_equipment_status`.

### `EquipmentDao.searchVisibleFor(User user, String keyword)`

Purpose: search visible equipment by asset tag, equipment name, or category.

Input:
- `keyword`

Output:
- `List<Equipment>`

### `EquipmentDao.create(...)` and `EquipmentDao.update(...)`

Purpose: admin equipment management.

Database changes:
- Inserts or updates one row in `equipment`.

### `EquipmentDao.retire(int equipmentId)`

Purpose: mark equipment as no longer bookable without deleting historical records.

Database changes:
- Sets `equipment.status` to `RETIRED`.

## Reservation

### `ReservationService.requestReservation(...)`

Purpose: create a pending reservation after validating time, equipment status, and conflicts.

Input:
- `equipmentIds`
- `requesterId`
- `start`
- `end`
- `purpose`
- `consumableRequests`

Output:
- new reservation id

Validation:
- end time must be after start time.
- purpose cannot be blank.
- the selected equipment item must be `AVAILABLE` or `RESERVED`.
- student requesters can only reserve equipment from their linked labs.
- overlapping `PENDING` or `APPROVED` reservations are not allowed.

Transaction:
- Uses one transaction for status check, conflict check, and insert.
- Writes the selected equipment to `reservations.equipment_id`.
- Writes optional consumable needs to `reservation_consumables`.

### `ReservationService.decideReservation(...)`

Purpose: approve or reject a pending reservation.

Input:
- `reservationId`
- `approverId`
- `approve`
- `comment`

Output:
- no return value

Database changes:
- Updates `reservations.status`.
- Inserts one row into `approvals`.

### `ReservationDao.cancel(int reservationId, int requesterId)`

Purpose: allow a user to cancel their own pending or approved reservation.

Database changes:
- Updates reservation status to `CANCELLED`.

## Maintenance

### `MaintenanceService.reportProblem(...)`

Purpose: report a fault and remove equipment from normal booking.

Input:
- `equipmentId`
- `reporterId`
- `title`
- `description`
- `priority`

Database changes:
- Inserts into `maintenance_tickets`.
- Updates `equipment.status` to `MAINTENANCE`.

Transaction:
- Ticket creation and equipment status update are committed together.

### `MaintenanceService.handleTicketAction(...)`

Purpose: handle the controlled maintenance actions from the web UI.

Input:
- `ticketId`
- `action` (`ACCEPT` or `RESOLVE`)
- `userId`

Database changes:
- `ACCEPT` assigns the current technician/admin and changes the ticket from `OPEN` to `IN_PROGRESS`.
- `RESOLVE` changes an assigned in-progress ticket to `RESOLVED`.
- Inserts into `maintenance_updates`.
- When no active ticket remains for the same equipment, equipment status is set back to `AVAILABLE`.

## Inventory

### `InventoryService.addConsumable(...)`

Purpose: create a new consumable item type for a lab.

Input:
- `labId`
- `itemName`
- `unit`
- `quantity`
- `reorderLevel`

Validation:
- item name and unit cannot be blank.
- quantity and reorder level cannot be negative.

### `InventoryService.changeStock(...)`

Purpose: change consumable stock and record the reason.

Input:
- `consumableId`
- `userId`
- `amount`
- `reason`

Validation:
- amount cannot be zero.
- reason cannot be blank.
- final stock quantity cannot be negative.

Transaction:
- Locks the consumable row.
- Updates current stock.
- Inserts transaction history.

## Reports

### `ReportDao.labUsage()`

Purpose: show reservation totals by lab.

Output:
- label: lab code
- count A: reservation count
- count B: approved count
- count C: completed count

Related SQL:
- Reads from `v_lab_usage_report`.

### `ReportDao.equipmentStatusSummary()`

Purpose: show equipment count, high-risk count, and open ticket count by equipment status.

Output:
- label: equipment status
- count A: equipment count
- count B: high-risk equipment count
- count C: open ticket count
