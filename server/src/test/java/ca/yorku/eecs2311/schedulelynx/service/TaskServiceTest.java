package ca.yorku.eecs2311.schedulelynx.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ca.yorku.eecs2311.schedulelynx.domain.Difficulty;
import ca.yorku.eecs2311.schedulelynx.domain.Task;
import ca.yorku.eecs2311.schedulelynx.domain.User;
import ca.yorku.eecs2311.schedulelynx.persistence.ScheduleEntryRepository;
import ca.yorku.eecs2311.schedulelynx.persistence.TaskRepository;
import ca.yorku.eecs2311.schedulelynx.persistence.UserRepository;
import java.lang.reflect.Constructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TaskServiceTest {

  private TaskRepository taskRepository;
  private UserRepository userRepository;
  private ScheduleEntryRepository scheduleEntryRepository;
  private TaskService taskService;

  @BeforeEach
  void setUp() {
    taskRepository = mock(TaskRepository.class);
    userRepository = mock(UserRepository.class);
    scheduleEntryRepository = mock(ScheduleEntryRepository.class);
    taskService = new TaskService(taskRepository, userRepository,
                                  scheduleEntryRepository);
  }

  @Test
  void create_shouldPersistTaskForUser() {
    User user = testUser(1L, "user1");

    Task request = new Task();
    request.setTitle("Finish lab");
    request.setDueDate(LocalDate.now().plusDays(2));
    request.setEstimatedHours(4);
    request.setDifficulty(Difficulty.HIGH);
    request.setPreferredStartTime(LocalTime.of(10, 0));
    request.setPreferredEndTime(LocalTime.of(16, 0));
    request.setMaxHoursPerDay(2);
    request.setMinBlockHours(1);
    request.setMaxBlockHours(2);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
      Task saved = inv.getArgument(0);
      ReflectionTestUtils.setField(saved, "id", 10L);
      return saved;
    });

    Task created = taskService.create(1L, request);

    assertNotNull(created);
    assertEquals(10L, created.getId());
    assertEquals("Finish lab", created.getTitle());
    assertEquals(LocalDate.now().plusDays(2), created.getDueDate());
    assertEquals(4, created.getEstimatedHours());
    assertEquals(Difficulty.HIGH, created.getDifficulty());
    assertEquals(LocalTime.of(10, 0), created.getPreferredStartTime());
    assertEquals(LocalTime.of(16, 0), created.getPreferredEndTime());
    assertEquals(2, created.getMaxHoursPerDay());
    assertEquals(1, created.getMinBlockHours());
    assertEquals(2, created.getMaxBlockHours());

    verify(userRepository).findById(1L);
    verify(taskRepository).save(any(Task.class));
    verify(scheduleEntryRepository).deleteAllByOwnerId(1L);
  }

  @Test
  void create_shouldApplyDefaultsWhenOptionalFieldsMissing() {
    User user = testUser(1L, "user1");

    Task request = new Task();
    request.setTitle("Defaulted task");
    request.setDueDate(LocalDate.now().plusDays(2));
    request.setEstimatedHours(3);
    request.setDifficulty(Difficulty.MEDIUM);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(taskRepository.save(any(Task.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    Task created = taskService.create(1L, request);

    assertEquals(LocalTime.of(9, 0), created.getPreferredStartTime());
    assertEquals(LocalTime.of(21, 0), created.getPreferredEndTime());
    assertEquals(3, created.getMaxHoursPerDay());
    assertEquals(1, created.getMinBlockHours());
    assertEquals(3, created.getMaxBlockHours());
  }

  @Test
  void create_shouldThrowWhenUserMissing() {
    Task request = new Task();
    request.setTitle("Task");
    request.setDueDate(LocalDate.now().plusDays(1));
    request.setEstimatedHours(2);
    request.setDifficulty(Difficulty.LOW);

    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
                 () -> taskService.create(1L, request));

    verify(userRepository).findById(1L);
    verify(taskRepository, never()).save(any());
  }

  @Test
  void getAll_shouldReturnOnlyTasksForUser() {
    Task t1 = new Task();
    ReflectionTestUtils.setField(t1, "id", 1L);
    t1.setTitle("Task A");

    Task t2 = new Task();
    ReflectionTestUtils.setField(t2, "id", 2L);
    t2.setTitle("Task B");

    when(taskRepository.findAllByOwnerIdOrderByDueDateAscIdAsc(1L))
        .thenReturn(List.of(t1, t2));

    List<Task> tasks = taskService.getAll(1L);

    assertEquals(2, tasks.size());
    assertEquals("Task A", tasks.get(0).getTitle());
    assertEquals("Task B", tasks.get(1).getTitle());

    verify(taskRepository).findAllByOwnerIdOrderByDueDateAscIdAsc(1L);
  }

  @Test
  void getById_shouldReturnTaskWhenOwnedByUser() {
    Task task = new Task();
    ReflectionTestUtils.setField(task, "id", 5L);
    task.setTitle("Owned task");

    when(taskRepository.findByIdAndOwnerId(5L, 1L))
        .thenReturn(Optional.of(task));

    Task found = taskService.getById(1L, 5L);

    assertEquals(5L, found.getId());
    assertEquals("Owned task", found.getTitle());

    verify(taskRepository).findByIdAndOwnerId(5L, 1L);
  }

  @Test
  void getById_shouldThrowWhenTaskNotFound() {
    when(taskRepository.findByIdAndOwnerId(99L, 1L))
        .thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
                 () -> taskService.getById(1L, 99L));

    verify(taskRepository).findByIdAndOwnerId(99L, 1L);
  }

  @Test
  void update_shouldModifyFieldsAndClearScheduleEntries() {
    Task existing = new Task();
    ReflectionTestUtils.setField(existing, "id", 7L);
    existing.setTitle("Old title");
    existing.setDueDate(LocalDate.now().plusDays(1));
    existing.setEstimatedHours(2);
    existing.setDifficulty(Difficulty.LOW);

    Task update = new Task();
    update.setTitle("New title");
    update.setDueDate(LocalDate.now().plusDays(4));
    update.setEstimatedHours(6);
    update.setDifficulty(Difficulty.HIGH);
    update.setPreferredStartTime(LocalTime.of(11, 0));
    update.setPreferredEndTime(LocalTime.of(17, 0));
    update.setMaxHoursPerDay(3);
    update.setMinBlockHours(1);
    update.setMaxBlockHours(2);

    when(taskRepository.findByIdAndOwnerId(7L, 1L))
        .thenReturn(Optional.of(existing));
    when(taskRepository.save(any(Task.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    Task updated = taskService.update(1L, 7L, update);

    assertEquals("New title", updated.getTitle());
    assertEquals(LocalDate.now().plusDays(4), updated.getDueDate());
    assertEquals(6, updated.getEstimatedHours());
    assertEquals(Difficulty.HIGH, updated.getDifficulty());
    assertEquals(LocalTime.of(11, 0), updated.getPreferredStartTime());
    assertEquals(LocalTime.of(17, 0), updated.getPreferredEndTime());
    assertEquals(3, updated.getMaxHoursPerDay());
    assertEquals(1, updated.getMinBlockHours());
    assertEquals(2, updated.getMaxBlockHours());

    verify(taskRepository).findByIdAndOwnerId(7L, 1L);
    verify(taskRepository).save(existing);
    verify(scheduleEntryRepository).deleteAllByOwnerId(1L);
  }

  @Test
  void update_shouldApplyDefaultsWhenOptionalFieldsMissing() {
    Task existing = new Task();
    ReflectionTestUtils.setField(existing, "id", 7L);
    existing.setTitle("Old");

    Task update = new Task();
    update.setTitle("Updated");
    update.setDueDate(LocalDate.now().plusDays(3));
    update.setEstimatedHours(5);
    update.setDifficulty(Difficulty.MEDIUM);

    when(taskRepository.findByIdAndOwnerId(7L, 1L))
        .thenReturn(Optional.of(existing));
    when(taskRepository.save(any(Task.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    Task updated = taskService.update(1L, 7L, update);

    assertEquals(LocalTime.of(9, 0), updated.getPreferredStartTime());
    assertEquals(LocalTime.of(21, 0), updated.getPreferredEndTime());
    assertEquals(3, updated.getMaxHoursPerDay());
    assertEquals(1, updated.getMinBlockHours());
    assertEquals(3, updated.getMaxBlockHours());
  }

  @Test
  void update_shouldThrowWhenTaskNotFound() {
    Task update = new Task();
    update.setTitle("Updated");
    update.setDueDate(LocalDate.now().plusDays(3));
    update.setEstimatedHours(5);
    update.setDifficulty(Difficulty.MEDIUM);

    when(taskRepository.findByIdAndOwnerId(7L, 1L))
        .thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
                 () -> taskService.update(1L, 7L, update));

    verify(taskRepository).findByIdAndOwnerId(7L, 1L);
    verify(taskRepository, never()).save(any());
  }

  @Test
  void delete_shouldDeleteTaskAndAssociatedScheduleEntries() {
    Task task = new Task();
    ReflectionTestUtils.setField(task, "id", 12L);
    task.setTitle("Delete me");

    when(taskRepository.findByIdAndOwnerId(12L, 1L))
        .thenReturn(Optional.of(task));

    taskService.delete(1L, 12L);

    verify(taskRepository).findByIdAndOwnerId(12L, 1L);
    verify(scheduleEntryRepository).deleteAllByTaskId(12L);
    verify(taskRepository).delete(task);
  }

  @Test
  void delete_shouldThrowWhenTaskNotFound() {
    when(taskRepository.findByIdAndOwnerId(50L, 1L))
        .thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
                 () -> taskService.delete(1L, 50L));

    verify(taskRepository).findByIdAndOwnerId(50L, 1L);
    verify(taskRepository, never()).delete(any());
    verify(scheduleEntryRepository, never()).deleteAllByTaskId(anyLong());
  }

  @Test
  void deleteAll_shouldDeleteAllUserTasksAndScheduleEntries() {
    taskService.deleteAll(1L);

    verify(scheduleEntryRepository).deleteAllByOwnerId(1L);
    verify(taskRepository).deleteAllByOwnerId(1L);
  }

  @Test
  void create_shouldThrowWhenEstimatedHoursInvalid() {
    User user = testUser(1L, "user1");

    Task request = new Task();
    request.setTitle("Bad task");
    request.setDueDate(LocalDate.now().plusDays(1));
    request.setEstimatedHours(0);
    request.setDifficulty(Difficulty.LOW);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    assertThrows(IllegalArgumentException.class,
                 () -> taskService.create(1L, request));

    verify(taskRepository, never()).save(any());
  }

  @Test
  void create_shouldThrowWhenPreferredTimeRangeInvalid() {
    User user = testUser(1L, "user1");

    Task request = new Task();
    request.setTitle("Bad task");
    request.setDueDate(LocalDate.now().plusDays(1));
    request.setEstimatedHours(2);
    request.setDifficulty(Difficulty.LOW);
    request.setPreferredStartTime(LocalTime.of(18, 0));
    request.setPreferredEndTime(LocalTime.of(17, 0));

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    assertThrows(IllegalArgumentException.class,
                 () -> taskService.create(1L, request));

    verify(taskRepository, never()).save(any());
  }

  private User testUser(Long id, String username) {
    try {
      Constructor<User> ctor = User.class.getDeclaredConstructor();
      ctor.setAccessible(true);
      User u = ctor.newInstance();
      ReflectionTestUtils.setField(u, "id", id);
      ReflectionTestUtils.setField(u, "username", username);
      ReflectionTestUtils.setField(u, "email", username + "@example.com");
      ReflectionTestUtils.setField(u, "fullName", "Test User");
      ReflectionTestUtils.setField(u, "passwordHash", "hash");
      ReflectionTestUtils.setField(u, "createdAt", LocalDateTime.now());
      return u;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
