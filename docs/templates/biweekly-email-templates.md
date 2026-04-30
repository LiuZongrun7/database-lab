# Biweekly Email Templates

Replace `Group XX`, names, dates, and actual work details before sending or attaching to the report.

## Week 10 Email Template

Subject:

`COMP2013J GroupXX Week10`

Email body:

Dear Jie Chen,

This is the Week 10 self-assessment and plan for Group XX.

Self-assessment for the previous two weeks:

During Weeks 8 and 9, our group confirmed the project topic as a Campus Laboratory Equipment Reservation and Maintenance System. We discussed the main users of the system, including students, teachers, technicians, and administrators. We created the first version of the requirements and identified the main database entities, such as users, laboratories, equipment, courses, reservations, approvals, maintenance tickets, consumables, and stock transactions. We also started the Java Maven project and prepared the first version of the MySQL database schema and seed data. The main challenge was controlling the project scope, because we wanted the system to be complex enough for a database assignment but still possible to complete within the semester.

Plan for the next two weeks:

For Weeks 10 and 11, our plan is to complete the reservation and maintenance workflows. We will implement login, equipment browsing, reservation request creation, reservation approval, maintenance ticket reporting, and ticket status updates. We will also add transaction logic for reservation conflict checking so that overlapping bookings are rejected. At the same time, we will continue improving the ER diagram, relational schema, and report structure. Each member will prepare notes for their own module so that the final interview can be shared by all members.

Best regards,

Group XX

## Week 12 Email Template

Subject:

`COMP2013J GroupXX Week12`

Email body:

Dear Jie Chen,

This is the Week 12 self-assessment and plan for Group XX.

Self-assessment for the previous two weeks:

During Weeks 10 and 11, we completed most of the core implementation. The reservation module can create pending requests, reject overlapping booking times, and support teacher or administrator approval. The maintenance module can create tickets and automatically move equipment into maintenance status. The inventory module supports stock changes and prevents negative stock values. We also added management reports based on SQL views and aggregation queries. The system now has a simple web interface with separate tabs for equipment, reservations, maintenance, inventory, and reports. The biggest improvement was connecting database rules with Java service transactions instead of only displaying static data.

Plan for the next two weeks:

For the final stage, we will focus on testing, documentation, and presentation. We will run automated tests and manual UI tests, update the report with screenshots and diagrams, prepare the video script, and practise answering database design questions. We also need to check that every member can explain their own module and understand the overall ER model, reservation transaction, and inventory transaction. Finally, we will package the source code and data in a zip file and submit the PDF report and video separately.

Best regards,

Group XX

## Individual Self-Assessment Examples

These are example paragraphs to include as appendices if your report includes individual reflection. Edit them according to real work.

### Member A Example

In the first stage, I worked on project coordination and the reservation workflow design. I helped set up the Maven project and planned how the UI should connect to the service layer. In the implementation stage, I focused on reservation creation, approval handling, and integration testing. I learned that reservation conflict checking must be handled carefully because the time overlap condition is easy to get wrong.

### Member B Example

I focused on database design and SQL implementation. I prepared the ER model, relational schema, constraints, indexes, views, and seed data. I also checked that the ER diagram matched the SQL tables. Through this work, I improved my understanding of many-to-many relationships, foreign keys, and why transaction history tables are useful.

### Member C Example

I worked on equipment management and maintenance workflow. I helped define equipment statuses and maintenance ticket statuses. I implemented or reviewed the logic where reporting a problem creates a maintenance ticket and changes equipment status to maintenance. This helped me understand how application logic and database state should be kept consistent.

### Member D Example

I worked on consumable inventory and report queries. I helped design the stock transaction table and the logic for preventing negative stock. I also prepared report queries for lab usage and equipment status summary. The main thing I learned was that storing only the current quantity is not enough when the system needs audit history.

### Member E Example

I worked on UI consistency, model classes, testing, and documentation. I prepared test cases for login, reservation conflicts, maintenance status changes, and inventory updates. I also helped organise the final report structure and video script. This helped me understand how testing can support database-related business rules.
