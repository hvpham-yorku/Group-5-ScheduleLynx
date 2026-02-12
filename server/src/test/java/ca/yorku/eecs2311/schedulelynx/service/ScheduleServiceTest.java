package ca.yorku.eecs2311.schedulelynx.service;

import static org.junit.jupiter.api.Assertions.*;

import ca.yorku.eecs2311.schedulelynx.domain.AvailabilityBlock;
import ca.yorku.eecs2311.schedulelynx.domain.Difficulty;
import ca.yorku.eecs2311.schedulelynx.domain.FixedEvent;
import ca.yorku.eecs2311.schedulelynx.domain.Task;
import ca.yorku.eecs2311.schedulelynx.domain.Weekday;
import ca.yorku.eecs2311.schedulelynx.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ScheduleServiceTest {

  @Test
  void schedule_respectsFixedEventGap_andSplitsTask() {
    // Build services with in-memory repos directly (fast unit-style test)
    TaskService taskService = new TaskService(new InMemoryTaskRepository());
    AvailabilityService availabilityService =
        new AvailabilityService(new InMemoryAvailabilityRepository());
    FixedEventService fixedEventService =
        new FixedEventService(new InMemoryFixedEventRepository());

    ScheduleService scheduleService = new ScheduleService(
        taskService, availabilityService, fixedEventService);

    availabilityService.create(new AvailabilityBlock(
        null, Weekday.MONDAY, LocalTime.of(18, 0), LocalTime.of(21, 0)));

    fixedEventService.create(new FixedEvent(null, "Class", Weekday.MONDAY,
                                            LocalTime.of(19, 0),
                                            LocalTime.of(20, 0)));

    taskService.create(new Task(null, "Study", LocalDate.of(2026, 2, 13), 2,
                                Difficulty.MEDIUM));

    ScheduleService.ScheduleResult result =
        scheduleService.generateWeeklyPlan();

    assertTrue(result.isFeasible());
    assertEquals(2, result.getBlocks().size());

    assertEquals(LocalTime.of(18, 0), result.getBlocks().get(0).getStart());
    assertEquals(LocalTime.of(19, 0), result.getBlocks().get(0).getEnd());

    assertEquals(LocalTime.of(20, 0), result.getBlocks().get(1).getStart());
    assertEquals(LocalTime.of(21, 0), result.getBlocks().get(1).getEnd());
  }
}
