package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.AvailabilityBlock;
import ca.yorku.eecs2311.schedulelynx.domain.Difficulty;
import ca.yorku.eecs2311.schedulelynx.domain.Task;
import ca.yorku.eecs2311.schedulelynx.domain.Weekday;
import ca.yorku.eecs2311.schedulelynx.persistence.InMemoryAvailabilityRepository;
import ca.yorku.eecs2311.schedulelynx.persistence.InMemoryEventRepository;
import ca.yorku.eecs2311.schedulelynx.persistence.InMemoryTaskRepository;
import ca.yorku.eecs2311.schedulelynx.web.dto.EventRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleServiceTest {

  @Test
  void schedule_respectsFixedEventGap_andSplitsTask() {
    // Build services with in-memory repos directly (fast unit-style test)
    TaskService taskService = new TaskService(new InMemoryTaskRepository());
    AvailabilityService availabilityService =
        new AvailabilityService(new InMemoryAvailabilityRepository());
    EventService eventService =
        new EventService(new InMemoryEventRepository());

    ScheduleService scheduleService = new ScheduleService(
        taskService, availabilityService, eventService);

    availabilityService.create(new AvailabilityBlock(
        null, Weekday.MONDAY, LocalTime.of(18, 0), LocalTime.of(21, 0)));

    var title = "Class";
    var day   = Weekday.MONDAY;
    var start = LocalTime.of(19, 0);
    var end   = LocalTime.of(20, 0);

    var evReq = new EventRequest(title, day, start, end);

    eventService.create(evReq);

    taskService.create(new Task(null, "Study", LocalDate.of(2026, 2, 13), 2,
                                Difficulty.MEDIUM));

    ScheduleService.ScheduleResult result =
        scheduleService.generateWeeklyPlan(LocalDate.of(2026, 2, 9));

    assertEquals(LocalTime.of(18, 0), result.getBlocks().get(0).getStart());
    assertEquals(LocalTime.of(19, 0), result.getBlocks().get(0).getEnd());

    assertEquals(LocalTime.of(20, 0), result.getBlocks().get(1).getStart());
    assertEquals(LocalTime.of(21, 0), result.getBlocks().get(1).getEnd());
  }

  @Test
  void v2_respectsDueDate_andDailyCap() {
    TaskService taskService = new TaskService(new InMemoryTaskRepository());
    AvailabilityService availabilityService =
        new AvailabilityService(new InMemoryAvailabilityRepository());
    EventService eventService =
        new EventService(new InMemoryEventRepository());

    ScheduleService scheduleService = new ScheduleService(
        taskService, availabilityService, eventService);

    // Week start Monday
    java.time.LocalDate weekStart = java.time.LocalDate.of(2026, 2, 2);

    // Availability: 18:00–22:00 every day (4h/day)
    for (Weekday d : Weekday.values()) {
      availabilityService.create(
          new AvailabilityBlock(null, d, java.time.LocalTime.of(18, 0),
                                java.time.LocalTime.of(22, 0)));
    }

    // Task A: 4h due Tuesday -> but cap is 3h/day, so it must use Mon + Tue
    taskService.create(new Task(null, "Task A",
                                java.time.LocalDate.of(2026, 2, 3), 4,
                                Difficulty.HIGH));

    // Generate schedule
    ScheduleService.ScheduleResult result =
        scheduleService.generateWeeklyPlan(weekStart);

    assertTrue(result.isFeasible(), result.getMessage());

    // All blocks for Task A must be on MONDAY or TUESDAY only
    var taskABlocks = result.getBlocks()
                          .stream()
                          .filter(b -> b.getTaskTitle().equals("Task A"))
                          .toList();

    assertFalse(taskABlocks.isEmpty());

    for (var b : taskABlocks) {
      assertTrue(b.getDay() == Weekday.MONDAY || b.getDay() == Weekday.TUESDAY,
                 "Task A scheduled after due day: " + b.getDay());
    }

    // Because daily cap is 3h/day, Task A (4h) must be split across >= 2 blocks
    assertTrue(taskABlocks.size() >= 2,
               "Expected Task A to be split due to daily cap");
  }
}
