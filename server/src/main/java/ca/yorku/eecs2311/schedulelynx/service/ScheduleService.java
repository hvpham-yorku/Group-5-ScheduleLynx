package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.*;
import ca.yorku.eecs2311.schedulelynx.logic.TimeBlockUtils;
import java.time.LocalTime;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {

  private final TaskService taskService;
  private final AvailabilityService availabilityService;
  private final FixedEventService fixedEventService;

  public ScheduleService(TaskService taskService,
                         AvailabilityService availabilityService,
                         FixedEventService fixedEventService) {
    this.taskService = taskService;
    this.availabilityService = availabilityService;
    this.fixedEventService = fixedEventService;
  }

  /**
   * Generate a weekly plan (v1): - Uses weekly availability minus weekly fixed
   * events to compute free blocks - Allocates tasks (sorted by due date) into
   * free blocks in chronological order - Splits tasks across multiple blocks if
   * needed
   */
  public ScheduleResult generateWeeklyPlan() {
    List<TimeBlock> availability =
        availabilityService.getAll()
            .stream()
            .map(b -> new TimeBlock(b.getDay(), b.getStart(), b.getEnd()))
            .toList();

    List<TimeBlock> fixed =
        fixedEventService.getAll()
            .stream()
            .map(e -> new TimeBlock(e.getDay(), e.getStart(), e.getEnd()))
            .toList();

    List<TimeBlock> freeBlocks =
        TimeBlockUtils.subtractFixedEvents(availability, fixed);

    List<Task> tasks = taskService.getAll()
                           .stream()
                           .sorted(Comparator.comparing(Task::getDueDate))
                           .toList();

    List<ScheduledTaskBlock> scheduled = new ArrayList<>();

    // Convert free blocks to mutable queue we can consume
    Deque<TimeBlock> freeQueue = new ArrayDeque<>(freeBlocks);

    for (Task task : tasks) {
      int minutesRemaining = task.getEstimatedHours() * 60;

      while (minutesRemaining > 0) {
        TimeBlock free = freeQueue.peekFirst();
        if (free == null) {
          return ScheduleResult.infeasible(
              "Not enough free time to schedule all tasks.", scheduled);
        }

        int freeMinutes = minutesBetween(free.getStart(), free.getEnd());
        int allocate = Math.min(minutesRemaining, freeMinutes);

        LocalTime blockStart = free.getStart();
        LocalTime blockEnd = blockStart.plusMinutes(allocate);

        scheduled.add(new ScheduledTaskBlock(
            free.getDay(), blockStart, blockEnd,
            task.getId() == null ? -1 : task.getId(), task.getTitle()));

        minutesRemaining -= allocate;

        // Update or consume free block
        if (allocate == freeMinutes) {
          freeQueue.removeFirst();
        } else {
          freeQueue.removeFirst();
          freeQueue.addFirst(
              new TimeBlock(free.getDay(), blockEnd, free.getEnd()));
        }
      }
    }

    return ScheduleResult.feasible("Schedule generated successfully.",
                                   scheduled);
  }

  private int minutesBetween(LocalTime start, LocalTime end) {
    return (int)java.time.Duration.between(start, end).toMinutes();
  }

  // Simple result wrapper used by controller
  public static class ScheduleResult {
    private final boolean feasible;
    private final String message;
    private final List<ScheduledTaskBlock> blocks;

    private ScheduleResult(boolean feasible, String message,
                           List<ScheduledTaskBlock> blocks) {
      this.feasible = feasible;
      this.message = message;
      this.blocks = blocks;
    }

    public static ScheduleResult feasible(String message,
                                          List<ScheduledTaskBlock> blocks) {
      return new ScheduleResult(true, message, blocks);
    }

    public static ScheduleResult infeasible(String message,
                                            List<ScheduledTaskBlock> blocks) {
      return new ScheduleResult(false, message, blocks);
    }

    public boolean isFeasible() { return feasible; }

    public String getMessage() { return message; }

    public List<ScheduledTaskBlock> getBlocks() { return blocks; }
  }
}
