package ca.yorku.eecs2311.schedulelynx.server.service;

import static org.junit.jupiter.api.Assertions.*;

import ca.yorku.eecs2311.schedulelynx.server.domain.*;
import ca.yorku.eecs2311.schedulelynx.server.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScheduleServiceTest {

  private static final long USER_ID = 1L;

  private ScheduleService scheduleService;
  private TaskService taskService;
  private AvailabilityService availabilityService;
  private EventService eventService;

  @BeforeEach
  void setup() {
    taskService = new TaskService(new InMemoryTaskRepository());
    availabilityService =
        new AvailabilityService(new InMemoryAvailabilityRepository());
    eventService = new EventService(new InMemoryEventRepository());

    scheduleService =
        new ScheduleService(taskService, availabilityService, eventService);
  }

  @Test
  void schedule_generates_blocks_when_time_available() {
    // Availability Monday 9-12
    availabilityService.create(
        USER_ID, new AvailabilityBlock(null, Weekday.MONDAY, LocalTime.of(9, 0),
                                       LocalTime.of(12, 0)));

    // Task due Monday, 2 hours
    taskService.create(USER_ID,
                       new Task(null, "Task A", LocalDate.of(2026, 2, 2), 2,
                                Difficulty.MEDIUM));

    var result =
        scheduleService.generateWeeklyPlan(USER_ID, LocalDate.of(2026, 2, 2));
    assertTrue(result.isFeasible());
    assertFalse(result.getBlocks().isEmpty());
  }

  @Test
  void schedule_infeasible_when_not_enough_time() {
    // Availability Monday 9-10 (1 hour)
    availabilityService.create(
        USER_ID, new AvailabilityBlock(null, Weekday.MONDAY, LocalTime.of(9, 0),
                                       LocalTime.of(10, 0)));

    // Task needs 3 hours
    taskService.create(USER_ID,
                       new Task(null, "Big Task", LocalDate.of(2026, 2, 2), 3,
                                Difficulty.HIGH));

    var result =
        scheduleService.generateWeeklyPlan(USER_ID, LocalDate.of(2026, 2, 2));
    assertFalse(result.isFeasible());
  }
}
