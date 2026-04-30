# Web Front-End Guide

## How This Matches The Assignment PDF

The assignment says the system can include a nice user interface, for example:

> Web, application, etc.

This project uses the `Web` option. The front-end is written with HTML, CSS, and JavaScript. The Java application starts a lightweight HTTP server at `http://localhost:8080`, serves the web pages, and provides JSON APIs for the browser.

## What To Call It In The Report

Use one of these phrases:

- Web front-end
- browser-based user interface
- HTML/CSS/JavaScript front-end
- Java back-end with a web UI
- lightweight Java HTTP server with JSON APIs

## Main Front-End Files

| File | Purpose |
| --- | --- |
| `src/main/resources/web/index.html` | Page structure: login, tabs, forms, tables, dialog. |
| `src/main/resources/web/style.css` | Visual style and responsive layout. |
| `src/main/resources/web/app.js` | Front-end logic, API calls, table rendering, form submission. |
| `src/main/java/edu/ucd/comp2013j/lab/web/LabWebServer.java` | Java HTTP server and API handlers. |
| `src/main/java/edu/ucd/comp2013j/lab/web/FormData.java` | Reads form data from browser requests. |
| `src/main/java/edu/ucd/comp2013j/lab/web/Json.java` | Small JSON helper for API responses. |

## Main Screens

### Login Screen

Front-end:

- `index.html`
- `app.js`

Back-end API:

- `POST /api/login`

Purpose:

- lets users enter username and password;
- returns user id, full name, and role;
- switches from login page to main dashboard.

### Equipment Page

Front-end:

- Equipment tab in `index.html`
- `loadEquipment()` in `app.js`

Back-end API:

- `GET /api/equipment`
- `GET /api/equipment?q=keyword`
- `POST /api/maintenance/report`

User actions:

- view equipment;
- search equipment;
- report equipment problem.

### Reservations Page

Back-end API:

- `GET /api/reservations?userId=...`
- `POST /api/reservations/create`
- `POST /api/reservations/decide`
- `POST /api/reservations/cancel`

User actions:

- create reservation request;
- view reservations;
- approve or reject if teacher/admin;
- cancel active reservation.

### Maintenance Page

Back-end API:

- `GET /api/maintenance`
- `GET /api/users/technicians`
- `POST /api/maintenance/update`

User actions:

- view tickets;
- assign technician;
- update ticket status.

### Inventory Page

Back-end API:

- `GET /api/inventory`
- `POST /api/inventory/change`

User actions:

- view consumable stock;
- change stock;
- see low-stock flags.

### Reports Page

Back-end API:

- `GET /api/reports/lab-usage`
- `GET /api/reports/equipment-status`

User actions:

- view lab usage report;
- view equipment status summary.

## Architecture Explanation

The browser front-end does not connect to the database directly. It sends HTTP requests to Java API endpoints. The API handlers call service classes, the service classes apply business rules and transactions, and DAO classes execute SQL through JDBC.

Example flow:

```text
Browser form
  -> POST /api/reservations/create
      -> LabWebServer
          -> ReservationService
              -> ReservationDao
                  -> MySQL database
```

## Report Paragraph Example

The system includes a web front-end implemented with HTML, CSS, and JavaScript. The front-end is served by a lightweight Java HTTP server and communicates with the back-end through JSON-style API endpoints. The main web pages include login, equipment catalogue, reservation management, maintenance tickets, inventory, and reports. This satisfies the assignment's user interface requirement because users can operate the database system through a browser instead of using SQL directly.

## Screenshots To Put In The Report

Take screenshots of:

1. Login screen.
2. Main web dashboard.
3. Equipment tab.
4. Reservation form.
5. Approval buttons.
6. Maintenance ticket update dialog.
7. Inventory page.
8. Reports page.

## Possible Interview Questions

### Why did you change to a web front-end?

Because our group has studied web development, so HTML, CSS, and JavaScript are easier for us to understand and explain. The assignment also explicitly gives Web as an example UI type.

### Did you use a complex MVC framework?

No. The project uses Java's built-in lightweight HTTP server, simple static web files, JDBC, and service/DAO classes. We avoided ORM and complicated MVC frameworks.

### How does the web page get data?

The JavaScript code uses `fetch()` to call Java API endpoints. The Java server reads or writes the MySQL database through JDBC.
