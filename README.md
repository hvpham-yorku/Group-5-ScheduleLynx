# ScheduleLynx

## 1. Project Description

ScheduleLynx is a rule-based schedule and workload management system designed to help students manage academic, professional, and personal responsibilities.

The system allows users to:

- add tasks with due dates, estimated effort, and scheduling preferences
- define weekly availability blocks
- add fixed and recurring events such as classes, labs, work shifts, or appointments
- automatically generate a weekly schedule
- modify and delete tasks and events
- detect invalid inputs and scheduling conflicts

The scheduling engine distributes workload into available time blocks before due dates using a rule-based time-blocking approach.

---

## 2. Description of Submission Contents

This submission includes:

### Backend (Spring Boot / Java)

Location:

    /server

Includes:

- domain models
- database-backed repositories
- service layer (business logic)
- REST controllers
- schedule generation engine
- user/session support
- unit tests
- integration tests

### Frontend

Location:

    /server/src/main/resources/static

Includes:

- UI for task management
- UI for event management
- UI for schedule preferences
- schedule visualization
- login/register interface
- forms for schedule generation settings

### Documentation

Location:

    /docs

Includes:

- project documentation
- architecture/design documentation
- database documentation
- testing documentation
- development log

---

## 3. Implemented Features (ITR2 Scope Including ITR1)

### Task Management

- create task
- retrieve tasks
- update task
- delete task
- input validation
- task-specific scheduling settings such as:
  - preferred time window
  - max hours per day
  - min block length
  - max block length

### Event Management

- create event
- retrieve events
- update event
- delete event
- support recurring events
- validate required fields

### Schedule Preferences

- save user-specific scheduling preferences
- support weekend scheduling toggle
- support quiet hours / unavailable hours

### Schedule Generation

- time-block tasks into available time
- avoid overlap with events
- split tasks across multiple blocks if necessary
- respect due dates
- use stored schedule preferences
- support feasibility result reporting

### Persistence

- persistent database-backed storage using H2
- data remains available after application restart

### Testing

- service-layer unit tests
- integration tests
- backend scheduling and persistence behavior verified through automated tests

All tests pass using:

    mvn clean test

---

## 4. Deployment Instructions

### Prerequisites

- Java 17+
- Maven 3.8+

### Run Backend

From project root:

    cd server

Windows:

    mvnw.cmd spring-boot:run

Mac/Linux:

    ./mvnw spring-boot:run

You can also use:

    mvn spring-boot:run

Server runs at:

    http://localhost:8080

### Run Tests

From project root:

    cd server

Windows:

    mvnw.cmd clean test

Mac/Linux:

    ./mvnw clean test

Or:

    mvn clean test

---

## 5. API Overview

### Tasks

- POST /api/tasks
- GET /api/tasks
- PUT /api/tasks/{id}
- DELETE /api/tasks/{id}

### Events

- POST /api/events
- GET /api/events
- PUT /api/events/{id}
- DELETE /api/events/{id}

### Availability

- POST /api/availability
- GET /api/availability
- PUT /api/availability/{id}
- DELETE /api/availability/{id}

### Schedule Preferences

- GET /api/schedule/preferences
- PUT /api/schedule/preferences

### Schedule

- POST /api/schedule/generate
- GET /api/schedule
- DELETE /api/schedule

### Authentication / Session Support

- login/register/session-related endpoints as implemented in backend

---

## 6. User Guide

1. Start the backend server.
2. Open the frontend in a browser.
3. Register or log in.
4. Add weekly availability blocks.
5. Add fixed or recurring events.
6. Add tasks with due dates and estimated effort.
7. Adjust schedule preferences if needed.
8. Generate schedule.
9. View the time-blocked weekly plan.
10. Update tasks, events, or preferences and regenerate the schedule when necessary.

---

## 7. ITR2 Improvements

The main additions and improvements for ITR2 are:

- persistent database-backed storage using H2
- improved backend integration using Spring Boot
- support for recurring events
- support for user-specific schedule preferences
- improved scheduling workflow and constraints
- more complete task and event editing/deletion support

---

## 8. Database Info

This version uses an H2 file-based database.

Main points:

- data is stored persistently
- data remains after restarting the application
- schema is updated automatically at startup
- tasks, events, users, schedule preferences, and generated schedule entries are stored in the database

Typical local database location:

    /server/data

More details are provided in:

    /docs/database.md

---

## 9. Testing Info

This version includes both unit and integration tests.

The tests help verify:

- service-layer business logic
- controller behavior
- scheduling logic
- persistence-related functionality

More details are provided in:

    /docs/testing.md

---

## 10. Known Limitations

Current limitations include:

- some UI feedback still uses simple browser-style messages
- frontend polish can still be improved
- duplicate task names may still be allowed depending on validation rules
- H2 is suitable, but not for full-scale production deployment

---

## 11. Future Improvements

Possible future improvements include:

- stronger validation for duplicate or conflicting input
- improved UI notifications
- more advanced schedule optimization
- stronger authentication/security features

---

## 12. Summary

ScheduleLynx is a web-based scheduling and workload planning system designed to help students organize tasks, events, and availability more effectively.

For ITR2, the project was improved with persistent storage, stronger backend integration, updated scheduling functionality.
