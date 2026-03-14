# Testing Documentation

## 1. Overview

This document describes the automated testing included in the ScheduleLynx project for ITR2.

The purpose of the tests is to verify that important backend logic works correctly and to reduce the chance of regressions when changes are made.

The current test suite focuses mainly on:

- service-layer logic
- integration behavior across backend layers
- persistence-related behavior
- schedule generation behavior

---

## 2. Types of Tests

### Unit Tests

Unit tests are used to test individual backend components in isolation, especially service-layer logic.

These tests help verify:

- business rules
- validation rules
- expected service behavior
- schedule-related logic under controlled conditions

### Integration Tests

Integration tests are used to verify that multiple backend components work correctly together.

These tests help verify:

- controller-to-service interaction
- service-to-repository interaction
- persistence behavior
- schedule generation workflow
- updates and deletes affecting stored schedule data

---

## 3. Current Test Files

The current repository includes the following main test files:

### Service / Unit Tests

- `ScheduleServiceTest`
- `TaskServiceTest`
- `SchedulePreferencesServiceTest`

### Integration Tests

- `ScheduleLynxIntegrationTest`

---

## 4. What the Tests Cover

The tests currently verify important backend behavior such as:

### Schedule generation

- feasible schedule generation
- partially feasible and infeasible schedule scenarios
- respecting scheduling constraints
- clearing and regenerating old schedule entries correctly

### Task behavior

- task creation, retrieval, update, and delete behavior
- validation and persistence of task-specific scheduling fields
- ensuring related generated schedule entries are handled correctly

### Schedule preferences

- saving and retrieving user-specific schedule preferences
- updating preferences correctly
- clearing existing generated schedule data when needed after changes

### Integration behavior

- interaction between controllers, services, repositories, and database-backed entities
- persistence of tasks, events, schedule preferences, and generated schedule entries
- correct behavior across multiple backend layers working together

---

## 5. Running the Tests

To run all tests, go to the `server` directory and execute:

```bash
mvn clean test
```
