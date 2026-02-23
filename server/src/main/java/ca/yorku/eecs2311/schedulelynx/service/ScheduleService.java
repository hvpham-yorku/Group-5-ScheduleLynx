package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.*;
import ca.yorku.eecs2311.schedulelynx.logic.TimeBlockUtils;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {

  private final TaskService taskService;
  private final AvailabilityService availabilityService;
  private final OneTimeEventService oneTimeEventService;

  public ScheduleService(TaskService taskService,
                         AvailabilityService availabilityService,
                         OneTimeEventService oneTimeEventService) {
    this.taskService = taskService;
    this.availabilityService = availabilityService;
    this.oneTimeEventService = oneTimeEventService;
  }

  public ScheduleResult generateWeeklyPlan(LocalDate weekStart) {
    if (weekStart == null) {
      throw new IllegalArgumentException("weekStart must not be null");
    }
    if (weekStart.getDayOfWeek() != java.time.DayOfWeek.MONDAY) {
      throw new IllegalArgumentException("weekStart must be a Monday");
    }

    // Max scheduled task minutes per day (balance rule)
    final int maxTaskMinutesPerDay = 3 * 60; // 3 hours/day

    // Build availability and fixed blocks as TimeBlocks
    List<TimeBlock> availability =
        availabilityService.getAll()
            .stream()
            .map(b -> new TimeBlock(b.getDay(), b.getStart(), b.getEnd()))
            .toList();

    List<TimeBlock> fixed =
        oneTimeEventService.getAll()
            .stream()
            .map(e -> new TimeBlock(e.getDay(), e.getStart(), e.getEnd()))
            .toList();

    // Free blocks for the whole week
    List<TimeBlock> freeBlocks =
        TimeBlockUtils.subtractFixedEvents(availability, fixed);

    // Track how many minutes already scheduled per day
    EnumMap<Weekday, Integer> usedMinutesPerDay = new EnumMap<>(Weekday.class);
    for (Weekday d : Weekday.values())
      usedMinutesPerDay.put(d, 0);

    // Sort tasks
    List<Task> tasks =
        taskService.getAll()
            .stream()
            .sorted(Comparator.comparing(Task::getDueDate)
                        .thenComparing(
                            (Task t) -> difficultyRank(t.getDifficulty())))
            .toList();

    List<ScheduledTaskBlock> scheduled = new ArrayList<>();

    for (Task task : tasks) {
      if (task.getId() == null) {
        return ScheduleResult.infeasible("Task missing id: " + task.getTitle(),
                                         scheduled);
      }

      int minutesRemaining = task.getEstimatedHours() * 60;

      // Determine latest allowed weekday for this task within the target week
      Weekday latestDayAllowed =
          latestAllowedDayInWeek(task.getDueDate(), weekStart);
      if (latestDayAllowed == null) {
        return ScheduleResult.infeasible(
            "Task '" + task.getTitle() + "' is due outside the selected week.",
            scheduled);
      }

      // Create a filtered queue of free blocks
      Deque<TimeBlock> candidateFree = new ArrayDeque<>(
          freeBlocks.stream()
              .filter(tb -> tb.getDay().ordinal() <= latestDayAllowed.ordinal())
              .toList());

      while (minutesRemaining > 0) {
        // Find the next usable block in the master freeBlocks list
        int idx = findNextUsableFreeBlockIndex(freeBlocks, latestDayAllowed,
                                               usedMinutesPerDay,
                                               maxTaskMinutesPerDay);
        if (idx == -1) {
          return ScheduleResult.infeasible(
              "Not enough free time before due date for task: " +
                  task.getTitle(),
              scheduled);
        }

        TimeBlock free = freeBlocks.get(idx);

        int freeMinutes = minutesBetween(free.getStart(), free.getEnd());

        // Respect daily cap
        int usedToday = usedMinutesPerDay.get(free.getDay());
        int remainingTodayCap = maxTaskMinutesPerDay - usedToday;
        if (remainingTodayCap <= 0) {
          freeBlocks.remove(idx);
          continue;
        }

        int allocate = Math.min(minutesRemaining,
                                Math.min(freeMinutes, remainingTodayCap));

        LocalTime blockStart = free.getStart();
        LocalTime blockEnd = blockStart.plusMinutes(allocate);

        scheduled.add(new ScheduledTaskBlock(free.getDay(), blockStart,
                                             blockEnd, task.getId(),
                                             task.getTitle()));

        minutesRemaining -= allocate;
        usedMinutesPerDay.put(free.getDay(), usedToday + allocate);

        // Update master freeBlocks
        if (allocate == freeMinutes) {
          freeBlocks.remove(idx);
        } else {
          freeBlocks.set(idx,
                         new TimeBlock(free.getDay(), blockEnd, free.getEnd()));
        }
      }
    }

    return ScheduleResult.feasible("Schedule generated successfully.",
                                   scheduled);
  }

  private int
  difficultyRank(ca.yorku.eecs2311.schedulelynx.domain.Difficulty d) {
    // Lower number higher priority
    return switch (d) {
      case HIGH -> 0;
      case MEDIUM -> 1;
      case LOW -> 2;
    };
  }

  private Weekday latestAllowedDayInWeek(LocalDate dueDate,
                                         LocalDate weekStart) {
    LocalDate weekEnd = weekStart.plusDays(6);
    if (dueDate.isBefore(weekStart) || dueDate.isAfter(weekEnd)) {
      return null;
    }
    int offset =
        (int)java.time.temporal.ChronoUnit.DAYS.between(weekStart, dueDate);
    return Weekday.values()[offset];
  }

  private int findNextUsableFreeBlockIndex(
      List<TimeBlock> freeBlocks, Weekday latestAllowed,
      EnumMap<Weekday, Integer> usedMinutesPerDay, int maxPerDay) {
    for (int i = 0; i < freeBlocks.size(); i++) {
      TimeBlock b = freeBlocks.get(i);
      if (b.getDay().ordinal() > latestAllowed.ordinal())
        continue;

      int used = usedMinutesPerDay.get(b.getDay());
      if (used >= maxPerDay)
        continue;

      return i;
    }
    return -1;
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
