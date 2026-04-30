# Development Plan

## Week 8

Goals:
- Confirm project topic.
- Identify users and core workflows.
- Draft requirements.
- Start ER model.

Planned tasks:
- Choose campus laboratory equipment reservation and maintenance as the system scenario.
- List student, teacher, technician, and admin roles.
- Draft the main entities: users, labs, equipment, reservations, approvals, maintenance tickets, consumables, stock transactions.
- Set up Git/Maven project.

## Week 9

Goals:
- Finish initial database design.
- Create basic Java project.

Planned tasks:
- Write schema SQL.
- Add seed data.
- Implement database initialisation code.
- Implement login and simple equipment listing.

## Week 10

Goals:
- Finish reservation workflow.
- Submit first self-assessment and next plan.

Planned tasks:
- Implement reservation creation.
- Add time conflict checking.
- Add approval and rejection.
- Begin web page tab layout.
- Start unit tests for login and reservation conflict.

## Week 11

Goals:
- Finish maintenance and inventory workflows.

Planned tasks:
- Implement maintenance ticket creation and update.
- Link maintenance status to equipment availability.
- Implement consumable stock management.
- Record stock transaction history.

## Week 12

Goals:
- Finish reports and second self-assessment.

Planned tasks:
- Add report views and aggregation queries.
- Finish UI pages.
- Write test document and interface document.
- Prepare report screenshots.

## Week 13

Goals:
- Final packaging and presentation preparation.

Planned tasks:
- Run all tests.
- Check the zip contains source code and data.
- Prepare video script.
- Practise live demo and Q&A.
- Update AI usage statement honestly.

## Risks and Mitigation

| Risk | Mitigation |
| --- | --- |
| Database schema becomes too simple | Include many-to-many tables, views, constraints, and transactions. |
| UI takes too much time | Use simple HTML/CSS/JavaScript with tables and dialogs, without a complex front-end framework. |
| Team members cannot explain the full system | Create member guides and practise Q&A. |
| Demo database becomes dirty after practice | The application recreates and seeds the MySQL database at startup. |
| Reservation conflict logic is questioned | Keep a unit test and clear explanation of overlapping time condition. |
