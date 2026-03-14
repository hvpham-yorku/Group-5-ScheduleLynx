## `docs/database.md`

````md
# Database Documentation

## 1. Overview

For ITR2, ScheduleLynx was improved from an earlier prototype into a persistent database application.

This means the system now saves important data in a database instead of only keeping it temporarily in memory during runtime.

---

## 2. Database Used

The project currently uses:

- **H2 Database**

H2 was chosen because it is:

- lightweight
- easy to set up
- easy to integrate with Spring Boot
- suitable for development

---

## 3. Persistence in ITR2

In this version of the project, persistence is used so that data is not lost every time the application restarts.

This is an important improvement from earlier development stages.

The application stores important information such as:

- users
- tasks
- events
- schedule preferences
- generated schedule entries

---

## 4. Database Mode

The project uses H2 in **file-based mode**.

This means:

- database data is written to disk
- data remains available after restart
- the application works like a persistent system rather than a temporary prototype

---

## 5. Database Location

The database files are created inside the `server/data/` directory.

Typical files may include:

- `schedulelynxdb.mv.db`
- `schedulelynxdb.trace.db`

These files are generated at runtime and are part of the local database state.

---

## 6. Schema Management

The application uses Spring Boot and Hibernate to manage the schema automatically.

This means that when the application starts:

- missing tables can be created
- schema updates can be applied automatically

This makes development easier for ITR2.

---

## 7. H2 Console

The H2 database console is available for inspecting the database during development.

It can typically be accessed at:

```text
http://localhost:8080/h2-console
```
````
