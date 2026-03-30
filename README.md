# ScheduleLynx

## 1. Project Description

ScheduleLynx is a schedule and workload management system designed to help students organize academic, work, and personal responsibilities in one place.

The system allows users to:

- create, edit, and delete tasks
- define availability blocks
- create fixed and recurring events
- save scheduling preferences
- generate a weekly schedule automatically
- view schedules visually in weekly and monthly formats
- print schedules for offline use
- distinguish tasks visually using task colors

The scheduling engine distributes work into available time blocks before due dates while respecting stored preferences and existing events.

---

## 2. Description of Submission Contents

This submission includes the material required for ITR2 and ITR3.

### Source Code

Location:

    /server

Includes:

- domain models
- persistence layer
- service layer
- REST controllers
- scheduling logic
- authentication/session-related backend support
- unit tests
- integration tests

### Frontend

Location:

    /server/src/main/resources/static

Includes:

- login/register pages
- task management interface
- event management interface
- availability management interface
- schedule preferences UI
- weekly schedule view
- monthly calendar view
- schedule printing support
- color-based task visualization

### Documentation

Location:

    /docs

Includes:

- planning documents
- architecture sketch / design artifacts
- database documentation
- testing documentation
- log file
- UML/design documentation
- customer meeting summaries

---

## 3. Features Implemented

### Core Features from Earlier Iterations

- task creation, update, deletion, and retrieval
- fixed event support
- recurring event support
- availability input and management
- user-specific schedule preferences
- automatic schedule generation
- validation of required input fields
- persistent database-backed storage

### ITR3 Features / Major Changes

- monthly calendar view added
- schedule printing feature added
- task colors added for better visual distinction
- GUI improvements for displaying schedules more clearly

---

## 4. Database

The project uses an H2 database for persistence.

Database-related material is documented in:

    /docs/database.md

The database allows the application to store data between runs and supports the backend scheduling workflow required for the project.

---

## 5. Testing

The project includes:

- unit tests for service/business logic
- integration tests for persistence and scheduling-related flows

Testing documentation is available in:

    /docs/testing.md

To run tests:

From the project root:

    cd server

Windows:

    mvnw.cmd clean test

Mac/Linux:

    ./mvnw clean test

---

## 6. Deployment Instructions

### Prerequisites

- Java 17 or newer
- Maven 3.8 or newer

### Run the Application

From the project root:

    cd server

Windows:

    mvnw.cmd spring-boot:run

Mac/Linux:

    ./mvnw spring-boot:run

You may also use:

    mvn spring-boot:run

Default local address:

    http://localhost:8080

---

## 7. User Guide

1. Start the backend server.
2. Open the application in the browser.
3. Register or log in.
4. Add availability blocks.
5. Add fixed or recurring events.
6. Add tasks with due dates and other scheduling details.
7. Adjust schedule preferences if needed.
8. Generate the schedule.
9. View the generated weekly schedule.
10. Use the monthly view for a broader calendar display.
11. Use the print feature to print the schedule.
12. Use task colors to distinguish tasks more easily.

---

## 8. API Overview

### Tasks

- POST `/api/tasks`
- GET `/api/tasks`
- PUT `/api/tasks/{id}`
- DELETE `/api/tasks/{id}`

### Events

- POST `/api/events`
- GET `/api/events`
- PUT `/api/events/{id}`
- DELETE `/api/events/{id}`

### Availability

- POST `/api/availability`
- GET `/api/availability`
- PUT `/api/availability/{id}`
- DELETE `/api/availability/{id}`

### Schedule Preferences

- GET `/api/schedule/preferences`
- PUT `/api/schedule/preferences`

### Schedule

- POST `/api/schedule/generate`
- GET `/api/schedule`
- DELETE `/api/schedule`

### Authentication / Session

- login/register/session-related endpoints as implemented in the backend

---

## 9. Major ITR3 Changes in Structure and Behaviour

### Behaviour Changes

The main behavioural changes in ITR3 are:

- users can now view schedules in a monthly format
- users can print schedules directly from the interface
- tasks are now shown with colors to improve readability
- schedule presentation is more visual and easier to interpret

### Structural / Codebase Changes

During ITR3, the project continued moving toward a more complete release version through:

- additional backend refinement
- frontend feature additions
- testing improvements
- refactoring and cleanup work
- documentation updates for release and demo preparation

---

## 10. Known Issues / Unresolved Items

At the time of submission, all issues indentified on GitHub still are active.

---

## 11. Final Notes

This repository contains the code, tests, and documents for the final ITR3 / Delivery 2 submission. The README is intended to help the TA understand:

- what is included in the submission
- how to run the system
- what features are implemented
- what changed during ITR3
