# Submission and Report Guide

## Required Files

The assignment requires separate files:

- PDF report;
- video;
- zip file containing code and data.

Do not put the PDF report or video inside the code zip.

## Code Zip

Create the source zip:

```bash
./package-source.sh
```

This creates:

`lab-equipment-system-source.zip`

You can rename it to:

`GroupXX_LabEquipmentSystem_CodeAndData.zip`

## Final Report

Use this template:

`docs/templates/final-report-template.md`

The report should include:

- system description;
- requirements;
- system design;
- UML diagrams;
- ER diagram;
- database design;
- CREATE TABLE statements or schema appendix;
- implementation explanation;
- UI screenshots;
- testing;
- team member contribution;
- self-assessment records;
- AI usage statement.

## UI Screenshot Checklist

The report should show that the project has a front-end/user interface.

Take screenshots of:

- login screen;
- main tabbed window;
- Equipment tab;
- Reservation tab and new request dialog;
- approval workflow;
- Maintenance tab;
- Inventory tab;
- Reports tab.

## Video

Use:

- `docs/10-video-script.md`
- `docs/templates/video-narration-template.md`

Recommended video structure:

1. Introduction and problem.
2. User roles.
3. Database design.
4. Reservation demo.
5. Maintenance demo.
6. Inventory and reports demo.
7. Testing and conclusion.

## Biweekly Reports

Use:

- `docs/08b-biweekly-reports-template.md`
- `docs/templates/biweekly-email-templates.md`

Important:

- Replace placeholders.
- Edit according to what the group actually did.
- Attach the final self-assessment records as report appendices.

## AI Usage Statement

Use:

- `docs/08-ai-usage-statement.md`
- `docs/templates/ai-statement-options.md`

Use an honest statement. A safe version is:

AI assistants were used during this project mainly to help draft documentation, organise report content, prepare diagram descriptions, and support parts of code generation and review. All AI-assisted materials were checked and edited by the team. The team ran automated tests and manually reviewed the main workflows.

## Before Submission

Run:

```bash
mvn test
DB_PASSWORD='your_mysql_root_password' mvn exec:java
```

Check:

- group number is correct;
- member names and student numbers are correct;
- contribution table is accurate;
- screenshots are included;
- AI statement is included;
- PDF, video, and zip are separate files.

To run the full MySQL integration tests, also pass the password:

```bash
DB_PASSWORD='your_mysql_root_password' mvn test
```

Without `DB_PASSWORD`, the project still compiles, but database integration tests are skipped because local passwords should not be stored in source code.
