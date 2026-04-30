# UML Modeling Document

The diagrams are written in Mermaid text so they can be pasted into tools such as Mermaid Live Editor or Markdown previewers.

## Use Case Diagram

```mermaid
usecaseDiagram
actor Student
actor Teacher
actor Technician
actor Admin

Student --> (Login)
Student --> (Search Equipment)
Student --> (Submit Reservation Request)
Student --> (Cancel Own Reservation)
Student --> (Report Equipment Problem)

Teacher --> (Login)
Teacher --> (View All Reservations)
Teacher --> (Approve or Reject Reservation)
Teacher --> (View Reports)

Technician --> (Login)
Technician --> (Update Maintenance Ticket)
Technician --> (Manage Consumable Stock)

Admin --> (Login)
Admin --> (View All Reservations)
Admin --> (Approve or Reject Reservation)
Admin --> (Update Maintenance Ticket)
Admin --> (Manage Consumable Stock)
Admin --> (View Reports)
```

## Class Diagram

```mermaid
classDiagram
class App
class Database {
  -String jdbcUrl
  +init()
  +getConnection()
}
class User {
  -int id
  -String username
  -String role
  +isAdmin()
  +isTeacher()
  +isTechnician()
}
class Equipment
class Reservation
class MaintenanceTicket
class Consumable
class UserDao
class EquipmentDao
class ReservationDao
class MaintenanceDao
class InventoryDao
class ReportDao
class AuthService
class ReservationService
class MaintenanceService
class InventoryService
class LabWebServer
class WebFrontEnd

App --> Database
App --> LabWebServer
WebFrontEnd --> LabWebServer
AuthService --> UserDao
ReservationService --> Database
ReservationService --> ReservationDao
MaintenanceService --> Database
MaintenanceService --> MaintenanceDao
InventoryService --> Database
InventoryService --> InventoryDao
UserDao --> User
EquipmentDao --> Equipment
ReservationDao --> Reservation
MaintenanceDao --> MaintenanceTicket
InventoryDao --> Consumable
LabWebServer --> AuthService
LabWebServer --> ReservationService
LabWebServer --> MaintenanceService
LabWebServer --> InventoryService
```

## Reservation Sequence Diagram

```mermaid
sequenceDiagram
actor Student
participant UI as Web Front-End
participant API as LabWebServer
participant Service as ReservationService
participant DAO as ReservationDao
participant DB as MySQL Database

Student->>UI: enter equipment, course, time, purpose
UI->>API: POST /api/reservations/create
API->>Service: requestReservation(...)
Service->>DB: begin transaction
Service->>DB: SELECT equipment status FOR UPDATE
Service->>DAO: hasTimeConflict(...)
DAO->>DB: count overlapping reservations
alt no conflict and available
  Service->>DAO: create reservation
  DAO->>DB: INSERT reservation as PENDING
  Service->>DB: commit
  UI-->>Student: show success message
else conflict or unavailable
  Service->>DB: rollback
  UI-->>Student: show error message
end
```

## Maintenance Sequence Diagram

```mermaid
sequenceDiagram
actor User
participant UI as Web Front-End
participant API as LabWebServer
participant Service as MaintenanceService
participant DAO as MaintenanceDao
participant DB as MySQL Database

User->>UI: report equipment problem
UI->>API: POST /api/maintenance/report
API->>Service: reportProblem(...)
Service->>DB: begin transaction
Service->>DAO: create ticket
DAO->>DB: INSERT maintenance_tickets
Service->>DB: UPDATE equipment SET status='MAINTENANCE'
Service->>DB: commit
UI-->>User: ticket created
```

## Activity Diagram: Reservation Approval

```mermaid
flowchart TD
    A[Teacher opens Reservation tab] --> B[Select pending reservation]
    B --> C{Approve?}
    C -->|Yes| D[Update reservation status to APPROVED]
    C -->|No| E[Update reservation status to REJECTED]
    D --> F[Insert approval record]
    E --> F
    F --> G[Refresh reservation table]
```
