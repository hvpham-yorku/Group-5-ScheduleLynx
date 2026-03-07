package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.Event;
import ca.yorku.eecs2311.schedulelynx.domain.RecurrenceType;
import ca.yorku.eecs2311.schedulelynx.domain.ScheduleEntry;
import ca.yorku.eecs2311.schedulelynx.domain.Task;
import ca.yorku.eecs2311.schedulelynx.domain.User;
import ca.yorku.eecs2311.schedulelynx.persistence.EventRepository;
import ca.yorku.eecs2311.schedulelynx.persistence.ScheduleEntryRepository;
import ca.yorku.eecs2311.schedulelynx.persistence.TaskRepository;
import ca.yorku.eecs2311.schedulelynx.persistence.UserRepository;
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

  private static final LocalTime DEFAULT_DAY_START = LocalTime.of(9, 0);
  private static final LocalTime DEFAULT_DAY_END = LocalTime.of(21, 0);
  private static final int DEFAULT_MAX_HOURS_PER_DAY = 6;
  private static final int DEFAULT_MAX_BLOCK_HOURS = 3;

  private final ScheduleEntryRepository scheduleEntryRepository;
  private final TaskRepository taskRepository;
  private final EventRepository eventRepository;
  private final UserRepository userRepository;

  public ScheduleService(ScheduleEntryRepository scheduleEntryRepository,
                         TaskRepository taskRepository,
                         EventRepository eventRepository,
                         UserRepository userRepository) {
    this.scheduleEntryRepository = scheduleEntryRepository;
    this.taskRepository = taskRepository;
    this.eventRepository = eventRepository;
    this.userRepository = userRepository;
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

    LocalDate startDate = request != null && request.startDate() != null
                              ? request.startDate()
                              : LocalDate.now();
    LocalTime dayStart = request != null && request.dayStartTime() != null
                             ? request.dayStartTime()
                             : DEFAULT_DAY_START;
    LocalTime dayEnd = request != null && request.dayEndTime() != null
                           ? request.dayEndTime()
                           : DEFAULT_DAY_END;
    int maxHoursPerDay = request != null && request.maxHoursPerDay() != null
                             ? request.maxHoursPerDay()
                             : DEFAULT_MAX_HOURS_PER_DAY;
    int maxBlockHours = request != null && request.maxBlockHours() != null
                            ? request.maxBlockHours()
                            : DEFAULT_MAX_BLOCK_HOURS;

    if (!dayEnd.isAfter(dayStart)) {
      throw new IllegalArgumentException(
          "Day end time must be after day start time");
    }
    if (maxHoursPerDay <= 0) {
      throw new IllegalArgumentException(
          "maxHoursPerDay must be greater than 0");
    }
    if (maxBlockHours <= 0) {
      throw new IllegalArgumentException(
          "maxBlockHours must be greater than 0");
    }

    scheduleEntryRepository.deleteAllByOwnerId(userId);

    List<Task> tasks =
        taskRepository.findAllByOwnerIdOrderByDueDateAscIdAsc(userId);
    List<Event> events =
        eventRepository.findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(userId);

    if (tasks.isEmpty()) {
      return new ScheduleGenerationResult(List.of(), List.of());
    }

    LocalDate maxDueDate = tasks.stream()
                               .map(Task::getDueDate)
                               .max(LocalDate::compareTo)
                               .orElse(startDate);

    Map<LocalDate, List<BusyWindow>> busyByDate = new HashMap<>();
    Map<LocalDate, Integer> scheduledHoursByDate = new HashMap<>();

    for (LocalDate date = startDate; !date.isAfter(maxDueDate);
         date = date.plusDays(1)) {
      busyByDate.put(date, new ArrayList<>());
      scheduledHoursByDate.put(date, 0);
    }

    for (Event event : events) {
      for (LocalDate date = startDate; !date.isAfter(maxDueDate);
           date = date.plusDays(1)) {
        if (occursOn(event, date)) {
          busyByDate.computeIfAbsent(date, d -> new ArrayList<>())
              .add(new BusyWindow(event.getStartTime(), event.getEndTime()));
        }
      }
    }

    List<ScheduleEntry> createdEntries = new ArrayList<>();
    List<String> warnings = new ArrayList<>();

    for (Task task : tasks) {
      int remainingHours = task.getEstimatedHours();
      LocalDate date = startDate;

      while (remainingHours > 0 && !date.isAfter(task.getDueDate())) {
        busyByDate.computeIfAbsent(date, d -> new ArrayList<>());
        scheduledHoursByDate.putIfAbsent(date, 0);

        int alreadyScheduledToday = scheduledHoursByDate.get(date);
        int remainingCapacityToday = maxHoursPerDay - alreadyScheduledToday;

        if (remainingCapacityToday > 0) {
          int desiredBlockHours = Math.min(
              Math.min(remainingHours, maxBlockHours), remainingCapacityToday);

          Slot slot = findAvailableSlot(busyByDate.get(date), dayStart, dayEnd,
                                        desiredBlockHours);

          if (slot == null && desiredBlockHours > 1) {
            for (int smaller = desiredBlockHours - 1; smaller >= 1; smaller--) {
              slot = findAvailableSlot(busyByDate.get(date), dayStart, dayEnd,
                                       smaller);
              if (slot != null) {
                desiredBlockHours = smaller;
                break;
              }
            }
          }

          if (slot != null) {
            ScheduleEntry entry = new ScheduleEntry(
                date, slot.start(), slot.end(), desiredBlockHours, task, owner);

            ScheduleEntry saved = scheduleEntryRepository.save(entry);
            createdEntries.add(saved);

            busyByDate.get(date).add(new BusyWindow(slot.start(), slot.end()));
            scheduledHoursByDate.put(date,
                                     alreadyScheduledToday + desiredBlockHours);
            remainingHours -= desiredBlockHours;
          }
        }

        date = date.plusDays(1);
      }

      if (remainingHours > 0) {
        warnings.add("Could not fully schedule task \"" + task.getTitle() +
                     "\" before " + task.getDueDate());
      }
    }

    List<ScheduleEntry> allEntries =
        scheduleEntryRepository.findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(
            userId);

    return new ScheduleGenerationResult(allEntries, warnings);
  }

  private Slot findAvailableSlot(List<BusyWindow> busyWindows,
                                 LocalTime dayStart, LocalTime dayEnd,
                                 int blockHours) {
    List<BusyWindow> sorted = new ArrayList<>(busyWindows);
    sorted.sort(Comparator.comparing(BusyWindow::start));

    LocalTime candidateStart = dayStart;
    LocalTime neededEnd = candidateStart.plusHours(blockHours);

    for (BusyWindow busy : sorted) {
      neededEnd = candidateStart.plusHours(blockHours);

      if (!neededEnd.isAfter(busy.start())) {
        return new Slot(candidateStart, neededEnd);
      }

      if (candidateStart.isBefore(busy.end())) {
        candidateStart = busy.end();
      }
    }

    neededEnd = candidateStart.plusHours(blockHours);
    if (!neededEnd.isAfter(dayEnd)) {
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

  public record ScheduleGenerationResult(List<ScheduleEntry> entries,
                                         List<String> warnings) {}
}
