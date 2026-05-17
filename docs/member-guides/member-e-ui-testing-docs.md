# Member E Guide - Web Front-End, Testing, and Documentation

## Your Claimed Area

Web front-end, role-based navigation, student registration, model classes, authentication support, tests, and documentation organisation.

## Files You Should Understand

- `src/main/resources/web/index.html`
- `src/main/resources/web/style.css`
- `src/main/resources/web/app.js`
- `src/main/java/edu/ucd/comp2013j/lab/web/LabWebServer.java`
- `src/main/java/edu/ucd/comp2013j/lab/web/FormData.java`
- `src/main/java/edu/ucd/comp2013j/lab/web/Json.java`
- `src/main/java/edu/ucd/comp2013j/lab/model/*.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/UserDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/dao/CourseDao.java`
- `src/main/java/edu/ucd/comp2013j/lab/service/AuthService.java`
- `src/test/java/edu/ucd/comp2013j/lab/SystemServiceTest.java`
- `docs/07-test-document.md`
- `docs/09-report-outline.md`
- `docs/10-video-script.md`

## What You Need To Understand

The front-end is built with HTML, CSS, and JavaScript:

- `index.html` contains the page structure, tabs, forms, and tables.
- `style.css` controls the visual style and responsive layout.
- `app.js` calls the Java API endpoints using `fetch()`, renders table rows, and handles form submissions.

The Java web server is in `LabWebServer.java`. It serves the static web files and provides API endpoints such as `/api/login`, `/api/equipment`, `/api/reservations/create`, `/api/maintenance/update`, and `/api/inventory/change`.

The current UI also includes student self-registration, role-specific tabs, a collapsed equipment picker with a selected-equipment summary, admin equipment controls, and an admin consumable creation button.

The browser does not directly access the database. The flow is:

```text
HTML form / JavaScript
  -> Java HTTP API
      -> Service class
          -> DAO class
              -> MySQL database
```

The automated tests check important behaviour:

- correct and wrong login;
- student registration;
- reservation conflict rejection;
- stock cannot go below zero;
- valid stock update changes quantity;
- consumable type creation;
- reporting maintenance changes equipment status.

## Manual Things You Should Do

- Run `mvn test`.
- Run `DB_PASSWORD='your_mysql_root_password' mvn exec:java`.
- Open `http://localhost:8080`.
- Test login and all tabs in the browser.
- Test student registration.
- Test that different roles see different tabs.
- Test equipment picker expand/collapse and selected summary.
- Take screenshots of the major web pages.
- Check the video script timing with the group.
- Make sure the AI usage statement is included and honest.

## Possible Viva Questions

Q: Why did you use a web front-end?

A: Our group has studied web development, and the assignment gives Web as an example user interface. A browser-based UI is easier for us to understand, test, and explain.

Q: Did you use a complicated web framework?

A: No. We used simple HTML, CSS, JavaScript, and Java's lightweight HTTP server. We avoided ORM and complicated MVC frameworks.

Q: How does the front-end call the back-end?

A: `app.js` uses `fetch()` to call API endpoints. The Java server handles the request, calls service classes, and the service classes use DAO classes to run SQL.

Q: Why does self-registration only create student accounts?

A: Admin, teacher, and technician accounts have higher permissions. In a real system, those accounts should be created or approved by an administrator.

Q: What is the purpose of the model classes?

A: They store data returned from the database in a readable Java form. This keeps API and service code cleaner than passing raw `ResultSet` objects around.

Q: What tests did you write?

A: Automated tests cover login, student registration, reservation conflict, inventory validation, inventory update, consumable creation, and maintenance status update.

Q: What documentation did you prepare?

A: Requirements, team division, UML, database design, interface/API document, development plan, test document, report outline, video script, and member guides.
