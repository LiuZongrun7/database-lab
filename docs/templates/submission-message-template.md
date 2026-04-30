# Submission Message and Final Checklist Templates

## Brightspace Submission Note Example

Project title: Campus Laboratory Equipment Reservation and Maintenance System

Group: Group XX

Submitted files:

- `GroupXX_Report.pdf`
- `GroupXX_Video.mp4`
- `lab-equipment-system-source.zip`

The source zip contains the Java Maven project, SQL schema, seed data, tests, and project documents. The report PDF and video are submitted separately as required.

## Final File Names Example

Use clear names:

- `GroupXX_LabEquipmentSystem_Report.pdf`
- `GroupXX_LabEquipmentSystem_Video.mp4`
- `GroupXX_LabEquipmentSystem_CodeAndData.zip`

## Final Checklist

Before submitting:

- Replace `Group XX` everywhere.
- Replace `Member A/B/C/D/E` with real names and student numbers.
- Check that the contribution table matches actual work.
- Edit the biweekly self-assessment records to match real progress.
- Add screenshots to the final PDF.
- Include ER diagram and UML diagrams.
- Include AI usage statement.
- Run `mvn test`.
- Run `DB_PASSWORD='your_mysql_root_password' mvn exec:java`.
- Test login accounts.
- Confirm the source zip includes `src`, `docs`, `pom.xml`, `README.md`, and SQL files.
- Confirm the source zip excludes `target` and generated database files.
- Submit report PDF, video, and zip as separate files.

## Demo Account Table For Report

| Username | Password | Role |
| --- | --- | --- |
| admin | admin123 | ADMIN |
| teacher | teacher123 | TEACHER |
| student1 | student123 | STUDENT |
| tech | tech123 | TECHNICIAN |

## Run Instructions For Report

The project can be run with Maven:

```bash
cd lab-equipment-system
DB_PASSWORD='your_mysql_root_password' mvn exec:java
```

On first start, the MySQL database is created if needed, then the tables are recreated and seeded automatically from:

- `src/main/resources/db/schema.sql`
- `src/main/resources/db/seed.sql`

## Short README Paragraph For Zip

This zip contains the source code and data for the Campus Laboratory Equipment Reservation and Maintenance System. It is a Java Maven project using a HTML/CSS/JavaScript web front-end, a lightweight Java HTTP server, JDBC, and MySQL. To run the project, install Java, Maven, and MySQL, then run `DB_PASSWORD='your_mysql_root_password' mvn exec:java` from the project root and open `http://localhost:8080`.
