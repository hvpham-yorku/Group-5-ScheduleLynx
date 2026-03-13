package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.Difficulty;
import ca.yorku.eecs2311.schedulelynx.domain.Event;
import ca.yorku.eecs2311.schedulelynx.domain.RecurrenceType;
import ca.yorku.eecs2311.schedulelynx.domain.ScheduleEntry;
import ca.yorku.eecs2311.schedulelynx.domain.Task;
import ca.yorku.eecs2311.schedulelynx.domain.User;
import ca.yorku.eecs2311.schedulelynx.domain.UserSchedulePreferences;
import ca.yorku.eecs2311.schedulelynx.persistence.EventRepository;
import ca.yorku.eecs2311.schedulelynx.persistence.ScheduleEntryRepository;
import ca.yorku.eecs2311.schedulelynx.persistence.TaskRepository;
import ca.yorku.eecs2311.schedulelynx.persistence.UserRepository;
import ca.yorku.eecs2311.schedulelynx.persistence.UserSchedulePreferencesRepository;
import ca.yorku.eecs2311.schedulelynx.web.dto.GenerateScheduleRequest;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleService {

  private static final LocalTime DEFAULT_TASK_START = LocalTime.of(9, 0);
  private static final LocalTime DEFAULT_TASK_END = LocalTime.of(21, 0);
  private static final int DEFAULT_TASK_MAX_HOURS_PER_DAY = 3;
  private static final int DEFAULT_TASK_MIN_BLOCK_HOURS = 1;
  private static final int DEFAULT_TASK_MAX_BLOCK_HOURS = 3;

  private final ScheduleEntryRepository scheduleEntryRepository;
  private final TaskRepository taskRepository;
  private final EventRepository eventRepository;
  private final UserRepository userRepository;
  private final UserSchedulePreferencesRepository preferencesRepository;

  public ScheduleService(
      ScheduleEntryRepository scheduleEntryRepository,
      TaskRepository taskRepository, EventRepository eventRepository,
      UserRepository userRepository,
      UserSchedulePreferencesRepository preferencesRepository) {
    this.scheduleEntryRepository = scheduleEntryRepository;
    this.taskRepository = taskRepository;
    this.eventRepository = eventRepository;
    this.userRepository = userRepository;
    this.preferencesRepository = preferencesRepository;
  }

  public List<ScheduleEntry> getAll(long userId) {
    return scheduleEntryRepository
        .findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(userId);
  }

  public List<ScheduleEntry> getBetween(long userId, LocalDate startDate,
                                        LocalDate endDate) {
    return scheduleEntryRepository
        .findAllByOwnerIdAndDateBetweenOrderByDateAscStartTimeAscIdAsc(
            userId, startDate, endDate);
  }

  @Transactional
  public void clear(long userId) {
    scheduleEntryRepository.deleteAllByOwnerId(userId);
  }

  @Transactional
  public ScheduleGenerationResult generate(long userId,
                                           GenerateScheduleRequest request) {
    User owner = userRepository.findById(userId).orElseThrow(
        () -> new IllegalArgumentException("User not found"));

    UserSchedulePreferences prefs =
        preferencesRepository.findByOwnerId(userId).orElseGet(() -> {
          UserSchedulePreferences p = new UserSchedulePreferences(owner);
          return preferencesRepository.save(p);
        });

    LocalDate startDate = request != null && request.startDate() != null
                              ? request.startDate()
                              : LocalDate.now();

    boolean allowWeekendScheduling =
        prefs.getAllowWeekendScheduling() == null ||
        prefs.getAllowWeekendScheduling();

    LocalTime quietHoursStart = prefs.getQuietHoursStart();
    LocalTime quietHoursEnd = prefs.getQuietHoursEnd();

    scheduleEntryRepository.deleteAllByOwnerId(userId);

    List<Task> tasks = new ArrayList<>(
        taskRepository.findAllByOwnerIdOrderByDueDateAscIdAsc(userId));
    List<Event> events =
        eventRepository.findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(userId);

    if (tasks.isEmpty()) {
      return new ScheduleGenerationResult(ScheduleStatus.FEASIBLE, List.of(),
                                          List.of());
    }

    tasks.sort(
        Comparator.comparing(Task::getDueDate)
            .thenComparing((Task t)
                               -> difficultyRank(t.getDifficulty()),
                           Comparator.reverseOrder())
            .thenComparing(Task::getEstimatedHours, Comparator.reverseOrder())
            .thenComparing(Task::getId));

    LocalDate maxDueDate = tasks.stream()
                               .map(Task::getDueDate)
                               .max(LocalDate::compareTo)
                               .orElse(startDate);

    Map<LocalDate, List<BusyWindow>> busyByDate = new HashMap<>();
    Map<Long, Map<LocalDate, Integer>> taskHoursByDate = new HashMap<>();
    List<String> warnings = new ArrayList<>();

    for (LocalDate date = startDate; !date.isAfter(maxDueDate);
         date = date.plusDays(1)) {
      busyByDate.put(date, new ArrayList<>());
    }

    for (Event event : events) {
      for (LocalDate date = startDate; !date.isAfter(maxDueDate);
           date = date.plusDays(1)) {
        if (occursOn(event, date)) {
          busyByDate.get(date).add(
              new BusyWindow(event.getStartTime(), event.getEndTime()));
        }
      }
    }

    if (quietHoursStart != null && quietHoursEnd != null &&
        !quietHoursStart.equals(quietHoursEnd)) {
      for (LocalDate date = startDate; !date.isAfter(maxDueDate);
           date = date.plusDays(1)) {
        if (quietHoursStart.isBefore(quietHoursEnd)) {
          busyByDate.get(date).add(
              new BusyWindow(quietHoursStart, quietHoursEnd));
        } else {
          busyByDate.get(date).add(
              new BusyWindow(LocalTime.MIN, quietHoursEnd));
          busyByDate.get(date).add(
              new BusyWindow(quietHoursStart, LocalTime.MAX));
        }
      }
    }

    List<ScheduleEntry> createdEntries = new ArrayList<>();
    int unscheduledTaskCount = 0;

    for (Task task : tasks) {
      int remainingHours = task.getEstimatedHours();

      LocalTime preferredStart = task.getPreferredStartTime() != null
                                     ? task.getPreferredStartTime()
                                     : DEFAULT_TASK_START;
      LocalTime preferredEnd = task.getPreferredEndTime() != null
                                   ? task.getPreferredEndTime()
                                   : DEFAULT_TASK_END;
      int maxHoursPerDay = task.getMaxHoursPerDay() != null
                               ? task.getMaxHoursPerDay()
                               : DEFAULT_TASK_MAX_HOURS_PER_DAY;
      int minBlockHours = task.getMinBlockHours() != null
                              ? task.getMinBlockHours()
                              : DEFAULT_TASK_MIN_BLOCK_HOURS;
      int maxBlockHours = task.getMaxBlockHours() != null
                              ? task.getMaxBlockHours()
                              : DEFAULT_TASK_MAX_BLOCK_HOURS;

      if (!preferredEnd.isAfter(preferredStart)) {
        warnings.add("Task \"" + task.getTitle() +
                     "\" has invalid preferred hours and was skipped.");
        unscheduledTaskCount++;
        continue;
      }

      LocalDate date = startDate;

      while (remainingHours > 0 && !date.isAfter(task.getDueDate())) {
        if (!allowWeekendScheduling && isWeekend(date)) {
          date = date.plusDays(1);
          continue;
        }

        taskHoursByDate.putIfAbsent(task.getId(), new HashMap<>());
        int alreadyScheduledForTaskToday =
            taskHoursByDate.get(task.getId()).getOrDefault(date, 0);

        int remainingCapacityToday =
            maxHoursPerDay - alreadyScheduledForTaskToday;

        if (remainingCapacityToday > 0) {
          int desiredBlockHours = Math.min(
              Math.min(remainingHours, maxBlockHours), remainingCapacityToday);

          Slot slot = null;

          for (int candidate = desiredBlockHours; candidate >= minBlockHours;
               candidate--) {
            slot = findAvailableSlot(busyByDate.get(date), preferredStart,
                                     preferredEnd, candidate);
            if (slot != null) {
              desiredBlockHours = candidate;
              break;
            }
          }

          if (slot == null && remainingHours < minBlockHours &&
              remainingCapacityToday >= remainingHours) {
            slot = findAvailableSlot(busyByDate.get(date), preferredStart,
                                     preferredEnd, remainingHours);
            if (slot != null) {
              desiredBlockHours = remainingHours;
            }
          }

          if (slot != null) {
            ScheduleEntry entry = new ScheduleEntry(
                date, slot.start(), slot.end(), desiredBlockHours, task, owner);

            ScheduleEntry saved = scheduleEntryRepository.save(entry);
            createdEntries.add(saved);

            busyByDate.get(date).add(new BusyWindow(slot.start(), slot.end()));
            taskHoursByDate.get(task.getId())
                .put(date, alreadyScheduledForTaskToday + desiredBlockHours);

            remainingHours -= desiredBlockHours;
          }
        }

        date = date.plusDays(1);
      }

      if (remainingHours > 0) {
        warnings.add("Could not fully schedule task \"" + task.getTitle() +
                     "\" before " + task.getDueDate());
        unscheduledTaskCount++;
      }
    }

    List<ScheduleEntry> allEntries =
        scheduleEntryRepository.findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(
            userId);

    ScheduleStatus status;
    if (unscheduledTaskCount == 0) {
      status = ScheduleStatus.FEASIBLE;
    } else if (!allEntries.isEmpty()) {
      status = ScheduleStatus.PARTIALLY_FEASIBLE;
    } else {
      status = ScheduleStatus.INFEASIBLE;
    }

    return new ScheduleGenerationResult(status, allEntries, warnings);
  }

  private int difficultyRank(Difficulty difficulty) {
    if (difficulty == null)
      return 0;
    return switch (difficulty) {
      case LOW -> 1;
      case MEDIUM -> 2;
      case HIGH -> 3;
    };
  }

  private boolean isWeekend(LocalDate date) {
    DayOfWeek day = date.getDayOfWeek();
    return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
  }

  private Slot findAvailableSlot(List<BusyWindow> busyWindows, LocalTime start,
                                 LocalTime end, int blockHours) {
    List<BusyWindow> sorted = new ArrayList<>(busyWindows);
    sorted.sort(Comparator.comparing(BusyWindow::start));

    LocalTime candidateStart = start;

    for (BusyWindow busy : sorted) {
      LocalTime neededEnd = candidateStart.plusHours(blockHours);

      if (!neededEnd.isAfter(busy.start())) {
        if (!neededEnd.isAfter(end)) {
          return new Slot(candidateStart, neededEnd);
        }
        return null;
      }

      if (candidateStart.isBefore(busy.end())) {
        candidateStart = busy.end();
      }
    }

    LocalTime neededEnd = candidateStart.plusHours(blockHours);
    if (!neededEnd.isAfter(end)) {
      return new Slot(candidateStart, neededEnd);
    }

    return null;
  }

  private boolean occursOn(Event event, LocalDate date) {
    if (date.isBefore(event.getDate())) {
      return false;
    }

    if (event.getRecurrenceEnd() != null &&
        date.isAfter(event.getRecurrenceEnd())) {
      return false;
    }

    if (!event.isRecurring()) {
      return event.getDate().equals(date);
    }

    RecurrenceType recurrenceType = event.getRecurrenceType();
    if (recurrenceType == null) {
      return event.getDate().equals(date);
    }

    if (recurrenceType == RecurrenceType.DAILY) {
      return true;
    }

    Set<DayOfWeek> recurrenceDays = event.getRecurrenceDays();
    DayOfWeek dateDay = date.getDayOfWeek();

    boolean dayMatches;
    if (recurrenceDays == null || recurrenceDays.isEmpty()) {
      dayMatches = dateDay == event.getDate().getDayOfWeek();
    } else {
      dayMatches = recurrenceDays.contains(dateDay);
    }

    if (!dayMatches) {
      return false;
    }

    if (recurrenceType == RecurrenceType.WEEKLY) {
      return true;
    }

    if (recurrenceType == RecurrenceType.BIWEEKLY) {
      long daysBetween = ChronoUnit.DAYS.between(event.getDate(), date);
      long weeksBetween = daysBetween / 7;
      return weeksBetween % 2 == 0;
    }

    return false;
  }

  private record BusyWindow(LocalTime start, LocalTime end) {}
  private record Slot(LocalTime start, LocalTime end) {}

  public enum ScheduleStatus { FEASIBLE, PARTIALLY_FEASIBLE, INFEASIBLE }

  public record ScheduleGenerationResult(ScheduleStatus status,
                                         List<ScheduleEntry> entries,
                                         List<String> warnings) {}
}
