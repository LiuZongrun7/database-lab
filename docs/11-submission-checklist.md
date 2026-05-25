# Submission Checklist

## Files Required by Assignment

- PDF report: submit as an individual file, not inside the source zip.
- Video: submit as an individual file, not inside the source zip.
- Source code and data zip: submit as a zip file.

## Source Zip

Recommended command:

```bash
./package-source.sh
```

This creates:

`lab-equipment-system-source.zip`

The zip includes:

- `pom.xml`
- `README.md`
- `run.sh`
- `src/main/java`
- `src/main/resources/db/schema.sql`
- `src/main/resources/db/seed.sql`
- `src/test/java`
- `docs`

The zip excludes:

- `target`
- generated local database/cache files
- macOS `.DS_Store` files

## Before Final Submission

- Replace placeholder group number and member names.
- Edit the team division document to match the real work.
- Edit the biweekly report templates to match the real work.
- Add screenshots to the PDF report.
- Include the AI usage statement honestly.
- Run `mvn test`.
- Run `DB_PASSWORD='your_mysql_root_password' mvn exec:java` and check the demo flow.
- Make sure every member reads their member guide and can answer questions.

## Demo Accounts

| Username | Password | Role |
| --- | --- | --- |
| admin | 123 | ADMIN |
| tech | 123 | TECHNICIAN |
| student_ai | 123 | STUDENT |
| student_bio | 123 | STUDENT |
| student_net | 123 | STUDENT |
| student_multi | 123 | STUDENT |
