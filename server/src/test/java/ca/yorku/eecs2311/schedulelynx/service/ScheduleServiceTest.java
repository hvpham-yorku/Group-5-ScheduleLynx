package ca.yorku.eecs2311.schedulelynx.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import ca.yorku.eecs2311.schedulelynx.domain.Difficulty;
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
import java.lang.reflect.Constructor;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

  @Mock private ScheduleEntryRepository scheduleEntryRepository;

  @Mock private TaskRepository taskRepository;

  @Mock private EventRepository eventRepository;

  @Mock private UserRepository userRepository;

  private ScheduleService scheduleService;

  @BeforeEach
  void setUp() {
    scheduleService =
        new ScheduleService(scheduleEntryRepository, taskRepository,
                            eventRepository, userRepository);
  }

  @Test
  void getAll_shouldDelegateToRepository() {
    long userId = 1L;

    when(
        scheduleEntryRepository.findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(
            userId))
        .thenReturn(List.of());

    List<ScheduleEntry> result = scheduleService.getAll(userId);

    assertNotNull(result);
    verify(scheduleEntryRepository)
        .findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(userId);
  }

  @Test
  void getBetween_shouldDelegateToRepository() {
    long userId = 1L;
    LocalDate start = LocalDate.of(2026, 3, 1);
    LocalDate end = LocalDate.of(2026, 3, 7);

    when(scheduleEntryRepository
             .findAllByOwnerIdAndDateBetweenOrderByDateAscStartTimeAscIdAsc(
                 userId, start, end))
        .thenReturn(List.of());

    List<ScheduleEntry> result = scheduleService.getBetween(userId, start, end);

    assertNotNull(result);
    verify(scheduleEntryRepository)
        .findAllByOwnerIdAndDateBetweenOrderByDateAscStartTimeAscIdAsc(
            userId, start, end);
  }

  @Test
  void clear_shouldDeleteAllScheduleEntriesForUser() {
    long userId = 1L;

    scheduleService.clear(userId);

    verify(scheduleEntryRepository).deleteAllByOwnerId(userId);
  }

  @Test
  void generate_shouldClearOldScheduleBeforeSavingNewOne() {
    long userId = 1L;
    User user = realUser(userId, "mykola");

    Task task = task(101L, user, "Rebuild schedule", LocalDate.of(2026, 3, 10),
                     2, Difficulty.MEDIUM);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(taskRepository.findAllByOwnerIdOrderByDueDateAscIdAsc(userId))
        .thenReturn(List.of(task));
    when(
        eventRepository.findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(userId))
        .thenReturn(List.of());
    when(scheduleEntryRepository.save(any(ScheduleEntry.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    GenerateScheduleRequest req = new GenerateScheduleRequest(
        LocalDate.of(2026, 3, 9), LocalTime.of(9, 0), LocalTime.of(17, 0), 6,
        2);

    ScheduleService.ScheduleGenerationResult result =
        scheduleService.generate(userId, req);

    assertNotNull(result);
    verify(scheduleEntryRepository).deleteAllByOwnerId(userId);
    verify(scheduleEntryRepository, atLeastOnce())
        .save(any(ScheduleEntry.class));
  }

  @Test
  void generate_shouldPersistEntriesLinkedToTasks() {
    long userId = 1L;
    User user = realUser(userId, "mykola");

    Task task = task(201L, user, "Persist test task", LocalDate.of(2026, 3, 10),
                     2, Difficulty.MEDIUM);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(taskRepository.findAllByOwnerIdOrderByDueDateAscIdAsc(userId))
        .thenReturn(List.of(task));
    when(
        eventRepository.findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(userId))
        .thenReturn(List.of());
    when(scheduleEntryRepository.save(any(ScheduleEntry.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    GenerateScheduleRequest req = new GenerateScheduleRequest(
        LocalDate.of(2026, 3, 9), LocalTime.of(9, 0), LocalTime.of(17, 0), 6,
        2);

    scheduleService.generate(userId, req);

    ArgumentCaptor<ScheduleEntry> captor =
        ArgumentCaptor.forClass(ScheduleEntry.class);
    verify(scheduleEntryRepository, atLeastOnce()).save(captor.capture());

    List<ScheduleEntry> savedEntries = captor.getAllValues();
    assertFalse(savedEntries.isEmpty());

    for (ScheduleEntry entry : savedEntries) {
      assertNotNull(entry.getTask());
      assertEquals(task.getId(), entry.getTask().getId());
      assertNotNull(entry.getOwner());
      assertEquals(userId, entry.getOwner().getId());
      assertNotNull(entry.getDate());
      assertNotNull(entry.getStartTime());
      assertNotNull(entry.getEndTime());
    }
  }

  @Test
  void generate_withRecurringEvents_shouldReturnResultWithoutCrashing() {
    long userId = 1L;
    User user = realUser(userId, "mykola");

    Task task = task(301L, user, "Study block", LocalDate.of(2026, 3, 12), 6,
                     Difficulty.MEDIUM);

    Event recurringLecture = recurringWeeklyEvent(
        401L, user, "Lecture", LocalDate.of(2026, 3, 2), LocalTime.of(9, 0),
        LocalTime.of(12, 0), LocalDate.of(2026, 4, 30),
        Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(taskRepository.findAllByOwnerIdOrderByDueDateAscIdAsc(userId))
        .thenReturn(List.of(task));
    when(
        eventRepository.findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(userId))
        .thenReturn(List.of(recurringLecture));
    when(scheduleEntryRepository.save(any(ScheduleEntry.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    GenerateScheduleRequest req = new GenerateScheduleRequest(
        LocalDate.of(2026, 3, 9), LocalTime.of(9, 0), LocalTime.of(18, 0), 6,
        3);

    ScheduleService.ScheduleGenerationResult result =
        scheduleService.generate(userId, req);

    assertNotNull(result);
    verify(scheduleEntryRepository).deleteAllByOwnerId(userId);
  }

  private User realUser(long id, String username) {
    try {
      Constructor<User> ctor = User.class.getDeclaredConstructor();
      ctor.setAccessible(true);
      User user = ctor.newInstance();

      ReflectionTestUtils.setField(user, "id", id);
      ReflectionTestUtils.setField(user, "username", username);
      ReflectionTestUtils.setField(user, "email", username + "@example.com");
      ReflectionTestUtils.setField(user, "fullName", "Test User");
      ReflectionTestUtils.setField(user, "passwordHash", "hashed-password");

      return user;
    } catch (Exception e) {
      throw new RuntimeException("Could not construct test User", e);
    }
  }

  private Task task(long id, User owner, String title, LocalDate dueDate,
                    int estimatedHours, Difficulty difficulty) {
    Task task = new Task();
    task.setId(id);
    task.setOwner(owner);
    task.setTitle(title);
    task.setDueDate(dueDate);
    task.setEstimatedHours(estimatedHours);
    task.setDifficulty(difficulty);
    return task;
  }

  private Event recurringWeeklyEvent(long id, User owner, String title,
                                     LocalDate date, LocalTime start,
                                     LocalTime end, LocalDate recurrenceEnd,
                                     Set<DayOfWeek> recurrenceDays) {
    Event event = new Event();
    event.setId(id);
    event.setOwner(owner);
    event.setTitle(title);
    event.setDate(date);
    event.setStartTime(start);
    event.setEndTime(end);
    event.setRecurring(true);
    event.setRecurrenceType(RecurrenceType.WEEKLY);
    event.setRecurrenceEnd(recurrenceEnd);
    event.setRecurrenceDays(recurrenceDays);
    return event;
  }
}
