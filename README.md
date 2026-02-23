# ScheduleLynx 

## 1. Project Description

ScheduleLynx is a rule-based schedule and workload management system
designed to help students manage academic, professional, and personal
responsibilities.

The system allows users to: - Add tasks with dueDates and estimated
effort - Define weekly availability blocks - Add fixed events (classes,
labs, shifts, etc.) - Automatically generate a weekly schedule - Modify
and delete tasks and events - Detect invalid inputs and scheduling
conflicts

The scheduling engine distributes workload into available time blocks
before dueDates using a rule-based time-blocking approach.

------------------------------------------------------------------------

## 2. Description of Submission Contents

This submission includes:

### Backend (Spring Boot -- Java)

Location:

    /server

Includes: - Domain models (Task, AvailabilityBlock, FixedEvent, User) -
In-memory repositories (fake database layer) - Service layer (business
logic) - REST controllers - Schedule generation engine - Unit tests
(service layer) - Controller integration tests

### Frontend

Location:

    /server/src/main/resources/static

Includes: - UI for task management - UI for availability and fixed
events - Schedule visualization - Basic login/register interface
(non-persistent for ITR1)

### Documentation

Location:

    /docs

Includes: - Project Planner (ITR0 document) - Architecture sketch -
Design documentation

------------------------------------------------------------------------

## 3. Implemented Features (ITR1 Scope)

### Task Management

-   Create task
-   Retrieve tasks
-   Update task
-   Delete task
-   Input validation

### Availability Management

-   Define weekly availability blocks
-   Prevent invalid time ranges

### Fixed Events

-   Add fixed events
-   Retrieve fixed events
-   Validate required fields

### Schedule Generation

-   Time block tasks into available time
-   Avoid overlap with fixed events
-   Split tasks across multiple blocks if necessary
-   Respect dueDates


### Testing

-   Service layer unit tests
-   Controller integration tests
-   Schedule engine tests

All tests pass using:

    mvn clean test

------------------------------------------------------------------------

## 4. Deployment Instructions

### Prerequisites

-   Java 17+
-   Maven 3.8+

### Run Backend

From project root:

    cd server
Windows:

    mvnw.cmd spring-boot:run
Mac/linux:

    ./mvnw spring-boot:run

Server runs at:

    http://localhost:8080

### Run Tests

    cd server
    
Windows:

    mvnw.cmd clean test
    
Mac/linux:

    ./mvnw clean test

------------------------------------------------------------------------

## 5. API Overview

### Tasks

-   POST /api/tasks
-   GET /api/tasks
-   PUT /api/tasks/{id}
-   DELETE /api/tasks/{id}

### Availability

-   POST /api/availability
-   GET /api/availability

### Fixed Events

-   POST /api/fixed-events
-   GET /api/fixed-events

### Schedule

-   GET /api/schedule?startDate=YYYY-MM-DD

------------------------------------------------------------------------

## 6. User Guide

1.  Start the backend server.
2.  Open the frontend in a browser.
3.  Add weekly availability blocks.
4.  Add fixed events (classes, shifts, etc.).
5.  Add tasks with dueDates and estimated effort.
6.  Generate schedule.
7.  View time-blocked weekly plan.

------------------------------------------------------------------------

## 7. Limitations 

-   No persistent database (in-memory only)
-   No secure authentication
-   No user specific task isolation
-   Data resets when server restarts

These features are planned for future iterations (ITR2+).

------------------------------------------------------------------------

## 8. Future Improvements

-   Persistent database 
-   Secure authentication 
-   User-specific data storage
-   Improved scheduling optimization
-   Enhanced UI/UX
