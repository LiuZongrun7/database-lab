# Final Report Outline

This outline follows the assignment requirements and can be used to write the final PDF report.

## Cover Page

- Module: COMP2013J Databases and Information Systems
- Project title: Campus Laboratory Equipment Reservation and Maintenance System
- Group number
- Member names and student numbers
- Submission date

## 1. System Description

Content to include:
- Background problem: shared lab equipment is hard to manage manually.
- System goal: manage reservations, approvals, equipment, maintenance, inventory, registration, and reports.
- Main users: student, teacher, technician, administrator.
- Assumptions and limitations.

Suggested paragraph:

The system is designed for a university laboratory environment where different courses and users share expensive equipment. It supports student registration, equipment browsing, multi-equipment reservation requests, approval decisions, maintenance tickets, consumable stock management, and management reports. The project is implemented with a Web front-end using HTML, CSS, and JavaScript, plus a Java JDBC back-end and a MySQL database.

## 2. Requirements

Include:
- Functional requirements from `docs/01-requirements.md`.
- Non-functional requirements.
- Scope limitations.

## 3. System Design

Include:
- Overall architecture: Web front-end, lightweight Java HTTP server, service layer, DAO layer, MySQL database.
- UML use case diagram.
- UML class diagram.
- Sequence diagrams for reservation and maintenance.

## 4. Database Design

Include:
- ER diagram.
- Entity descriptions.
- Relational schema.
- Explanation of many-to-many relationships.
- CREATE TABLE statements or selected important examples.
- Constraints and indexes.
- Views and report queries.
- Transaction design.

Important SQL topics to highlight:
- primary keys;
- foreign keys;
- check constraints;
- unique constraints;
- indexes;
- joins;
- views;
- aggregation;
- transactions;
- `FOR UPDATE` row locking.

## 5. Implementation

Include:
- Technology choices: Java, HTML/CSS/JavaScript, lightweight HTTP server, JDBC, Maven, MySQL.
- Package structure.
- Main modules:
  - authentication;
  - equipment catalogue;
  - admin equipment management;
  - reservation;
  - maintenance;
  - inventory;
  - reports.
- Screenshots of each front-end UI tab.

## 6. Testing

Include:
- Automated test summary.
- Manual test table.
- Screenshots or descriptions of successful tests.
- Known limitations.

Use `docs/07-test-document.md` as the source.

## 7. Team Member Contribution

Use the real final contribution. The planning version is in `docs/02-team-division.md`.

Suggested table columns:
- Member
- Main responsibility
- Files/modules
- Documentation contribution
- Testing/demo contribution

## 8. Self-Assessment and Self-Feedback Records

Attach the edited Week 10 and Week 12 records as appendices.

Source draft:
- `docs/08b-biweekly-reports-template.md`

## 9. AI Usage Statement

Include the final AI statement from:

- `docs/08-ai-usage-statement.md`

Make sure this section is honest and matches how the project was actually completed.

## 10. Conclusion

Summarise:
- what the system achieved;
- what database concepts were used;
- what could be improved in a future version.

Possible future improvements:
- hashed passwords;
- email notifications;
- QR code check-in;
- equipment image upload;
- web version;
- role permission table instead of hard-coded roles.

## Appendices

- Appendix A: Full schema SQL.
- Appendix B: Seed data.
- Appendix C: Weekly self-assessment records.
- Appendix D: Main test cases.
- Appendix E: Screenshots.
