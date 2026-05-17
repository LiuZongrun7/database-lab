# Biweekly Self-Assessment and Plan Templates

These are draft templates for the Week 10 and Week 12 emails. They must be edited to match the real work completed by the actual team. Do not submit them as fake records.

Suggested email subjects:

- `COMP2013J GroupXX Week10`
- `COMP2013J GroupXX Week12`

## Week 10 Group Self-Assessment Draft

During Weeks 8 and 9, our group confirmed the project topic as a campus laboratory equipment reservation and maintenance system. We discussed the main users of the system, including students, teachers, technicians, and administrators. We created the first version of the requirements and identified the main database entities, such as users, laboratories, equipment, courses, reservations, approvals, maintenance tickets, and consumable stock. We also started the Java Maven project and prepared the initial MySQL database schema and seed data. The main difficulty was deciding the correct project scope, because we wanted the system to be complex enough for a database assignment but still possible to finish within the semester.

## Week 10 Group Plan Draft

For Weeks 10 and 11, our plan is to complete the main reservation and maintenance workflows. We will implement login, equipment browsing, reservation request creation, reservation approval, maintenance ticket reporting, and ticket updates. We will also add transaction logic for reservation conflict checking so that overlapping bookings are rejected. At the same time, we will continue improving the ER diagram, relational schema, and report structure. Each member will prepare notes for their own module so that the final interview is not only based on the group leader.

## Week 10 Individual Self-Assessment Drafts

### Member A

I worked on project coordination and the reservation workflow plan. I helped decide the system scenario, listed the main reservation use cases, and started the Maven project structure. I also checked how the UI should connect to the service layer. My main issue was that the time conflict logic needed to be precise, so I planned to test it carefully in the next stage.

### Member B

I focused on database design. I drafted the ER model and mapped the main entities to relational tables. I also considered primary keys, foreign keys, and many-to-many tables such as course members and equipment-course access. My main challenge was keeping the schema complex enough without adding unnecessary tables.

### Member C

I worked on the equipment and maintenance part of the system. I listed equipment statuses and maintenance ticket statuses and considered how reporting a fault should affect equipment availability. I also helped prepare seed data for different labs and equipment types.

### Member D

I worked on consumable inventory and report requirements. I identified that the system should not only store current stock but also record stock transaction history. I also planned the report page, including lab usage and equipment status summaries.

### Member E

I worked on UI planning, testing ideas, and documentation structure. I helped organise the documents required by the assignment, including requirements, database design, UML, interface document, and test document. I also prepared early manual test cases for login and reservation.

## Week 12 Group Self-Assessment Draft

During Weeks 10 and 11, we completed most of the implementation. The reservation module can now create pending requests, support multiple equipment items in one request, record consumable needs, reject overlapping times, and support teacher/admin approval. The maintenance module can create tickets and automatically move equipment into maintenance status. The inventory module supports new consumable item types, stock changes, and negative-stock prevention. We also added management reports based on SQL views and aggregation queries. The system now has a web interface with role-based tabs for equipment, reservations, maintenance, inventory, and reports. Our biggest improvement was connecting database rules with Java service transactions instead of only displaying static data.

## Week 12 Group Plan Draft

For the final stage, we will focus on testing, documentation, and presentation. We will run automated tests and manual UI tests, update the report with screenshots and diagrams, prepare the video script, and practise answering database design questions. We also need to check that every member can explain their own module and at least understand the overall ER model, reservation transaction, and inventory transaction. Finally, we will package the source code and data in a zip file and submit the PDF report and video separately.

## Week 12 Individual Self-Assessment Drafts

### Member A

I implemented and tested the reservation workflow, including request creation, multi-equipment selection, consumable requests, and approval handling. I paid special attention to the overlapping time condition because it is an important database logic problem. I also helped integrate the UI tabs and prepared run instructions.

### Member B

I finalised the database schema and seed data. I added constraints, indexes, views, and link tables such as `reservation_equipment` and `reservation_consumables` to support the main system functions. I also checked that the ER diagram matches the SQL schema and prepared explanations for the relational mapping.

### Member C

I implemented the equipment and maintenance workflow. I added equipment management actions such as edit and retire, and connected maintenance ticket creation with equipment status updates, so faulty equipment cannot continue to be booked. I also prepared the maintenance part of the demonstration.

### Member D

I implemented the inventory and report modules. The inventory module supports adding consumable item types, records stock transactions, and uses transaction logic to prevent invalid stock values. I also created report queries for lab usage and equipment status summaries.

### Member E

I worked on UI consistency, role-based navigation, student registration, model classes, shared UI helpers, and testing documentation. I wrote automated tests for login, student registration, reservation conflict, inventory changes, consumable creation, and maintenance status changes. I also prepared manual test cases and the video script draft.
