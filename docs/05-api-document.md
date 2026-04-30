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
| `/api/equipment` | GET | List or search equipment. |
| `/api/courses` | GET | List courses visible to current user. |
| `/api/users/technicians` | GET | List technicians for maintenance assignment. |
| `/api/reservations` | GET | List reservations visible to current user. |
| `/api/reservations/create` | POST | Create a reservation request. |
| `/api/reservations/decide` | POST | Approve or reject a reservation. |
| `/api/reservations/cancel` | POST | Cancel user's own active reservation. |
| `/api/maintenance` | GET | List maintenance tickets. |
| `/api/maintenance/report` | POST | Report equipment problem. |
| `/api/maintenance/update` | POST | Update maintenance ticket. |
| `/api/inventory` | GET | List consumable stock. |
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

## Equipment

### `EquipmentDao.findAll()`

Purpose: return all equipment with lab and open ticket count.

Output:
- `List<Equipment>`

Related SQL:
- Reads from `v_equipment_status`.

### `EquipmentDao.search(String keyword)`

Purpose: search by asset tag, equipment name, or category.

Input:
- `keyword`

Output:
- `List<Equipment>`

## Reservation

### `ReservationService.requestReservation(...)`

Purpose: create a pending reservation after validating time, equipment status, and conflicts.

Input:
- `equipmentId`
- `requesterId`
- `courseId`
- `start`
- `end`
- `purpose`

Output:
- new reservation id

Validation:
- end time must be after start time.
- purpose cannot be blank.
- equipment must be `AVAILABLE` or `RESERVED`.
- overlapping `PENDING` or `APPROVED` reservations are not allowed.

Transaction:
- Uses one transaction for status check, conflict check, and insert.

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

### `MaintenanceService.updateTicket(...)`

Purpose: assign or update a maintenance ticket.

Input:
- `ticketId`
- `technicianId`
- `status`
- `userId`
- `note`

Database changes:
- Updates ticket technician and status.
- Inserts into `maintenance_updates`.
- If status is `RESOLVED` or `CLOSED`, equipment status is set back to `AVAILABLE`.

## Inventory

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
