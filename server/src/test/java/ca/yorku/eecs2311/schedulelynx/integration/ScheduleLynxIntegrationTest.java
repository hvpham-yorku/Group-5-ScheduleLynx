package ca.yorku.eecs2311.schedulelynx.integration;

import static org.junit.jupiter.api.Assertions.*;

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
import ca.yorku.eecs2311.schedulelynx.service.SchedulePreferencesService;
import ca.yorku.eecs2311.schedulelynx.service.ScheduleService;
import ca.yorku.eecs2311.schedulelynx.service.TaskService;
import ca.yorku.eecs2311.schedulelynx.web.dto.GenerateScheduleRequest;
import java.lang.reflect.Constructor;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ScheduleLynxIntegrationTest {

  @Autowired private UserRepository userRepository;

  @Autowired private TaskRepository taskRepository;

  @Autowired private EventRepository eventRepository;

  @Autowired private ScheduleEntryRepository scheduleEntryRepository;

  @Autowired private UserSchedulePreferencesRepository preferencesRepository;

  @Autowired private TaskService taskService;

  @Autowired private ScheduleService scheduleService;

  @Autowired private SchedulePreferencesService schedulePreferencesService;

  private User user;

  @BeforeEach
  void setUp() {
    scheduleEntryRepository.deleteAll();
    eventRepository.deleteAll();
    taskRepository.deleteAll();
    preferencesRepository.deleteAll();
    userRepository.deleteAll();

    user = createAndSaveUser("integration_user");
  }

  @Test
  void taskSpecificSchedulingFields_shouldPersistInDatabase() {
    Task task = new Task();
    task.setTitle("Physics assignment");
    task.setDueDate(LocalDate.now().plusDays(3));
    task.setEstimatedHours(6);
    task.setDifficulty(Difficulty.HIGH);
    task.setPreferredStartTime(LocalTime.of(14, 0));
    task.setPreferredEndTime(LocalTime.of(18, 0));
    task.setMaxHoursPerDay(2);
    task.setMinBlockHours(1);
    task.setMaxBlockHours(2);

    Task saved = taskService.create(user.getId(), task);

    Task loaded = taskRepository.findById(saved.getId()).orElseThrow();

    assertEquals("Physics assignment", loaded.getTitle());
    assertEquals(Difficulty.HIGH, loaded.getDifficulty());
    assertEquals(LocalTime.of(14, 0), loaded.getPreferredStartTime());
    assertEquals(LocalTime.of(18, 0), loaded.getPreferredEndTime());
    assertEquals(2, loaded.getMaxHoursPerDay());
    assertEquals(1, loaded.getMinBlockHours());
    assertEquals(2, loaded.getMaxBlockHours());
  }

  @Test
  void schedulePreferences_shouldPersistPerUser() {
    UserSchedulePreferences updated = schedulePreferencesService.update(
        user.getId(), false, LocalTime.of(22, 30), LocalTime.of(7, 30));

    assertNotNull(updated.getId());
    assertEquals(false, updated.getAllowWeekendScheduling());
    assertEquals(LocalTime.of(22, 30), updated.getQuietHoursStart());
    assertEquals(LocalTime.of(7, 30), updated.getQuietHoursEnd());

    UserSchedulePreferences loaded =
        preferencesRepository.findByOwnerId(user.getId()).orElseThrow();

    assertEquals(false, loaded.getAllowWeekendScheduling());
    assertEquals(LocalTime.of(22, 30), loaded.getQuietHoursStart());
    assertEquals(LocalTime.of(7, 30), loaded.getQuietHoursEnd());
  }

  @Test
  void generateSchedule_shouldRespectTaskPreferredTimeWindow() {
    schedulePreferencesService.update(user.getId(), true, LocalTime.of(23, 0),
                                      LocalTime.of(8, 0));

    Task task = new Task();
    task.setTitle("Embedded systems study");
    task.setDueDate(LocalDate.now().plusDays(2));
    task.setEstimatedHours(4);
    task.setDifficulty(Difficulty.HIGH);
    task.setPreferredStartTime(LocalTime.of(14, 0));
    task.setPreferredEndTime(LocalTime.of(18, 0));
    task.setMaxHoursPerDay(2);
    task.setMinBlockHours(1);
    task.setMaxBlockHours(2);

    taskService.create(user.getId(), task);

    ScheduleService.ScheduleGenerationResult result = scheduleService.generate(
        user.getId(), new GenerateScheduleRequest(LocalDate.now()));

    assertNotNull(result);
    assertFalse(result.entries().isEmpty());

    List<ScheduleEntry> entries =
        scheduleEntryRepository.findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(
            user.getId());

    assertFalse(entries.isEmpty());

    for (ScheduleEntry entry : entries) {
      assertFalse(entry.getStartTime().isBefore(LocalTime.of(14, 0)));
      assertFalse(entry.getEndTime().isAfter(LocalTime.of(18, 0)));
    }
  }

  @Test
  void generateSchedule_shouldRespectTaskMaxHoursPerDay() {
    schedulePreferencesService.update(user.getId(), true, LocalTime.of(23, 0),
                                      LocalTime.of(8, 0));

    Task task = new Task();
    task.setTitle("Project report");
    task.setDueDate(LocalDate.now().plusDays(3));
    task.setEstimatedHours(6);
    task.setDifficulty(Difficulty.MEDIUM);
    task.setPreferredStartTime(LocalTime.of(9, 0));
    task.setPreferredEndTime(LocalTime.of(21, 0));
    task.setMaxHoursPerDay(2);
    task.setMinBlockHours(1);
    task.setMaxBlockHours(2);

    taskService.create(user.getId(), task);

    scheduleService.generate(user.getId(),
                             new GenerateScheduleRequest(LocalDate.now()));

    List<ScheduleEntry> entries =
        scheduleEntryRepository.findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(
            user.getId());

    assertFalse(entries.isEmpty());

    HashMap<LocalDate, Integer> hoursPerDay = new HashMap<>();
    for (ScheduleEntry entry : entries) {
      hoursPerDay.merge(entry.getDate(), entry.getPlannedHours(), Integer::sum);
    }

    for (Integer total : hoursPerDay.values()) {
      assertTrue(total <= 2, "A task exceeded its maxHoursPerDay");
    }
  }

  @Test
  void generateSchedule_shouldRespectWeekendPreference() {
    schedulePreferencesService.update(user.getId(), false, LocalTime.of(23, 0),
                                      LocalTime.of(8, 0));

    LocalDate nextFriday = next(DayOfWeek.FRIDAY);

    Task task = new Task();
    task.setTitle("Weekend check task");
    task.setDueDate(nextFriday.plusDays(4));
    task.setEstimatedHours(6);
    task.setDifficulty(Difficulty.HIGH);
    task.setPreferredStartTime(LocalTime.of(9, 0));
    task.setPreferredEndTime(LocalTime.of(17, 0));
    task.setMaxHoursPerDay(2);
    task.setMinBlockHours(1);
    task.setMaxBlockHours(2);

    taskService.create(user.getId(), task);

    scheduleService.generate(user.getId(),
                             new GenerateScheduleRequest(nextFriday));

    List<ScheduleEntry> entries =
        scheduleEntryRepository.findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(
            user.getId());

    assertFalse(entries.isEmpty());

    for (ScheduleEntry entry : entries) {
      DayOfWeek dow = entry.getDate().getDayOfWeek();
      assertNotEquals(DayOfWeek.SATURDAY, dow);
      assertNotEquals(DayOfWeek.SUNDAY, dow);
    }
  }

  @Test
  void generateSchedule_shouldRespectRecurringEvents() {
    schedulePreferencesService.update(user.getId(), true, LocalTime.of(23, 0),
                                      LocalTime.of(8, 0));

    Task task = new Task();
    task.setTitle("Signals revision");
    task.setDueDate(LocalDate.now().plusDays(7));
    task.setEstimatedHours(4);
    task.setDifficulty(Difficulty.HIGH);
    task.setPreferredStartTime(LocalTime.of(9, 0));
    task.setPreferredEndTime(LocalTime.of(17, 0));
    task.setMaxHoursPerDay(2);
    task.setMinBlockHours(1);
    task.setMaxBlockHours(2);

    taskService.create(user.getId(), task);

    LocalDate monday = next(DayOfWeek.MONDAY);

    Event event = new Event();
    event.setTitle("Lecture");
    event.setOwner(user);
    event.setDate(monday);
    event.setStartTime(LocalTime.of(9, 0));
    event.setEndTime(LocalTime.of(12, 0));
    event.setRecurring(true);
    event.setRecurrenceType(RecurrenceType.WEEKLY);
    event.setRecurrenceEnd(monday.plusWeeks(4));
    event.setRecurrenceDays(Set.of(DayOfWeek.MONDAY));

    eventRepository.save(event);

    scheduleService.generate(user.getId(),
                             new GenerateScheduleRequest(LocalDate.now()));

    List<ScheduleEntry> entries =
        scheduleEntryRepository.findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(
            user.getId());

    assertFalse(entries.isEmpty());

    for (ScheduleEntry entry : entries) {
      boolean isBlockedMondayMorning =
          entry.getDate().getDayOfWeek() == DayOfWeek.MONDAY &&
          entry.getStartTime().isBefore(LocalTime.of(12, 0));

      assertFalse(isBlockedMondayMorning,
                  "Scheduler placed work during a recurring event");
    }
  }

  @Test
  void updatingTask_shouldClearOldGeneratedSchedule() {
    schedulePreferencesService.update(user.getId(), true, LocalTime.of(23, 0),
                                      LocalTime.of(8, 0));

    Task task = new Task();
    task.setTitle("Original task");
    task.setDueDate(LocalDate.now().plusDays(2));
    task.setEstimatedHours(4);
    task.setDifficulty(Difficulty.MEDIUM);
    task.setPreferredStartTime(LocalTime.of(9, 0));
    task.setPreferredEndTime(LocalTime.of(17, 0));
    task.setMaxHoursPerDay(2);
    task.setMinBlockHours(1);
    task.setMaxBlockHours(2);

    Task saved = taskService.create(user.getId(), task);

    scheduleService.generate(user.getId(),
                             new GenerateScheduleRequest(LocalDate.now()));
    assertFalse(
        scheduleEntryRepository
            .findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(user.getId())
            .isEmpty());

    Task updated = new Task();
    updated.setTitle("Updated task");
    updated.setDueDate(LocalDate.now().plusDays(3));
    updated.setEstimatedHours(5);
    updated.setDifficulty(Difficulty.HIGH);
    updated.setPreferredStartTime(LocalTime.of(10, 0));
    updated.setPreferredEndTime(LocalTime.of(16, 0));
    updated.setMaxHoursPerDay(2);
    updated.setMinBlockHours(1);
    updated.setMaxBlockHours(2);

    taskService.update(user.getId(), saved.getId(), updated);

    List<ScheduleEntry> entriesAfterUpdate =
        scheduleEntryRepository.findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(
            user.getId());

    assertTrue(entriesAfterUpdate.isEmpty(),
               "Schedule should be cleared after task update");
  }

  @Test
  void generateSchedule_shouldReturnPartiallyFeasibleWhenWorkCannotFullyFit() {
    schedulePreferencesService.update(user.getId(), false, LocalTime.of(22, 0),
                                      LocalTime.of(8, 0));

    LocalDate startDate = next(DayOfWeek.FRIDAY);

    Task task = new Task();
    task.setTitle("Impossible task");
    task.setDueDate(startDate);
    task.setEstimatedHours(10);
    task.setDifficulty(Difficulty.HIGH);
    task.setPreferredStartTime(LocalTime.of(20, 0));
    task.setPreferredEndTime(LocalTime.of(21, 0));
    task.setMaxHoursPerDay(1);
    task.setMinBlockHours(1);
    task.setMaxBlockHours(1);

    taskService.create(user.getId(), task);

    ScheduleService.ScheduleGenerationResult result = scheduleService.generate(
        user.getId(), new GenerateScheduleRequest(startDate));

    assertNotNull(result);
    assertEquals(ScheduleService.ScheduleStatus.PARTIALLY_FEASIBLE,
                 result.status());
    assertNotNull(result.warnings());
    assertFalse(result.warnings().isEmpty());

    List<ScheduleEntry> entries =
        scheduleEntryRepository.findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(
            user.getId());

    assertFalse(entries.isEmpty(),
                "Some schedule entries should be created before time runs out");

    int totalPlanned =
        entries.stream().mapToInt(ScheduleEntry::getPlannedHours).sum();

    assertTrue(totalPlanned < 10, "The task should not be fully scheduled");
  }

  @Test
  void generateSchedule_shouldReturnPartiallyFeasibleWhenOnlySomeWorkFits() {
    schedulePreferencesService.update(user.getId(), true, LocalTime.of(23, 0),
                                      LocalTime.of(8, 0));

    LocalDate startDate = LocalDate.now();

    Task fitTask = new Task();
    fitTask.setTitle("Fits completely");
    fitTask.setDueDate(startDate.plusDays(2));
    fitTask.setEstimatedHours(2);
    fitTask.setDifficulty(Difficulty.MEDIUM);
    fitTask.setPreferredStartTime(LocalTime.of(9, 0));
    fitTask.setPreferredEndTime(LocalTime.of(12, 0));
    fitTask.setMaxHoursPerDay(2);
    fitTask.setMinBlockHours(1);
    fitTask.setMaxBlockHours(2);

    Task hardTask = new Task();
    hardTask.setTitle("Does not fully fit");
    hardTask.setDueDate(startDate.plusDays(1));
    hardTask.setEstimatedHours(8);
    hardTask.setDifficulty(Difficulty.HIGH);
    hardTask.setPreferredStartTime(LocalTime.of(20, 0));
    hardTask.setPreferredEndTime(LocalTime.of(21, 0));
    hardTask.setMaxHoursPerDay(1);
    hardTask.setMinBlockHours(1);
    hardTask.setMaxBlockHours(1);

    taskService.create(user.getId(), fitTask);
    taskService.create(user.getId(), hardTask);

    ScheduleService.ScheduleGenerationResult result = scheduleService.generate(
        user.getId(), new GenerateScheduleRequest(startDate));

    assertNotNull(result);
    assertEquals(ScheduleService.ScheduleStatus.PARTIALLY_FEASIBLE,
                 result.status());
    assertNotNull(result.warnings());
    assertFalse(result.warnings().isEmpty());

    List<ScheduleEntry> entries =
        scheduleEntryRepository.findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(
            user.getId());

    assertFalse(
        entries.isEmpty(),
        "Some entries should still be created for a partially feasible plan");
  }

  @Test
  void deletingTask_shouldRemoveAssociatedScheduleEntries() {
    schedulePreferencesService.update(user.getId(), true, LocalTime.of(23, 0),
                                      LocalTime.of(8, 0));

    Task task = new Task();
    task.setTitle("Delete me");
    task.setDueDate(LocalDate.now().plusDays(2));
    task.setEstimatedHours(4);
    task.setDifficulty(Difficulty.MEDIUM);
    task.setPreferredStartTime(LocalTime.of(9, 0));
    task.setPreferredEndTime(LocalTime.of(17, 0));
    task.setMaxHoursPerDay(2);
    task.setMinBlockHours(1);
    task.setMaxBlockHours(2);

    Task saved = taskService.create(user.getId(), task);

    scheduleService.generate(user.getId(),
                             new GenerateScheduleRequest(LocalDate.now()));

    List<ScheduleEntry> beforeDelete =
        scheduleEntryRepository.findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(
            user.getId());
    assertFalse(beforeDelete.isEmpty(),
                "Schedule entries should exist before deleting the task");

    taskService.delete(user.getId(), saved.getId());

    List<ScheduleEntry> afterDelete =
        scheduleEntryRepository.findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(
            user.getId());
    assertTrue(
        afterDelete.isEmpty(),
        "Deleting the task should remove its associated schedule entries");
  }

  @Test
  void recurringEvent_shouldPersistWithRecurrenceFields() {
    LocalDate startDate = next(DayOfWeek.MONDAY);

    Event event = new Event();
    event.setTitle("Weekly lecture");
    event.setOwner(user);
    event.setDate(startDate);
    event.setStartTime(LocalTime.of(10, 0));
    event.setEndTime(LocalTime.of(12, 0));
    event.setRecurring(true);
    event.setRecurrenceType(RecurrenceType.WEEKLY);
    event.setRecurrenceEnd(startDate.plusWeeks(6));
    event.setRecurrenceDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));

    Event saved = eventRepository.save(event);

    Event loaded = eventRepository.findById(saved.getId()).orElseThrow();

    assertEquals("Weekly lecture", loaded.getTitle());
    assertTrue(loaded.isRecurring());
    assertEquals(RecurrenceType.WEEKLY, loaded.getRecurrenceType());
    assertEquals(startDate.plusWeeks(6), loaded.getRecurrenceEnd());
    assertNotNull(loaded.getRecurrenceDays());
    assertEquals(2, loaded.getRecurrenceDays().size());
    assertTrue(loaded.getRecurrenceDays().contains(DayOfWeek.MONDAY));
    assertTrue(loaded.getRecurrenceDays().contains(DayOfWeek.WEDNESDAY));
  }

  private User createAndSaveUser(String username) {
    try {
      Constructor<User> ctor = User.class.getDeclaredConstructor();
      ctor.setAccessible(true);
      User u = ctor.newInstance();

      ReflectionTestUtils.setField(u, "username", username);
      ReflectionTestUtils.setField(u, "email", username + "@example.com");
      ReflectionTestUtils.setField(u, "fullName", "Integration Test User");
      ReflectionTestUtils.setField(u, "passwordHash", "hashed-password");
      ReflectionTestUtils.setField(u, "createdAt", LocalDateTime.now());

      return userRepository.save(u);
    } catch (Exception e) {
      throw new RuntimeException("Could not construct test User", e);
    }
  }

  private LocalDate next(DayOfWeek target) {
    LocalDate date = LocalDate.now();
    while (date.getDayOfWeek() != target) {
      date = date.plusDays(1);
    }
    return date;
  }
}
