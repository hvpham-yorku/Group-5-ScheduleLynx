package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.Task;
import ca.yorku.eecs2311.schedulelynx.domain.User;
import ca.yorku.eecs2311.schedulelynx.persistence.ScheduleEntryRepository;
import ca.yorku.eecs2311.schedulelynx.persistence.TaskRepository;
import ca.yorku.eecs2311.schedulelynx.persistence.UserRepository;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

  private static final LocalTime DEFAULT_TASK_START = LocalTime.of(9, 0);
  private static final LocalTime DEFAULT_TASK_END = LocalTime.of(21, 0);
  private static final int DEFAULT_TASK_MAX_HOURS_PER_DAY = 3;
  private static final int DEFAULT_TASK_MIN_BLOCK_HOURS = 1;
  private static final int DEFAULT_TASK_MAX_BLOCK_HOURS = 3;

  private final TaskRepository taskRepository;
  private final UserRepository userRepository;
  private final ScheduleEntryRepository scheduleEntryRepository;

  public TaskService(TaskRepository taskRepository,
                     UserRepository userRepository,
                     ScheduleEntryRepository scheduleEntryRepository) {
    this.taskRepository = taskRepository;
    this.userRepository = userRepository;
    this.scheduleEntryRepository = scheduleEntryRepository;
  }

  public List<Task> getAll(long userId) {
    return taskRepository.findAllByOwnerIdOrderByDueDateAscIdAsc(userId);
  }

  public Task getById(long userId, long taskId) {
    return taskRepository.findByIdAndOwnerId(taskId, userId)
        .orElseThrow(() -> new IllegalArgumentException("Task not found"));
  }

  @Transactional
  public Task create(long userId, Task task) {
    User owner = userRepository.findById(userId).orElseThrow(
        () -> new IllegalArgumentException("User not found"));

    applyDefaultsAndValidate(task);
    task.setOwner(owner);

    Task saved = taskRepository.save(task);
    scheduleEntryRepository.deleteAllByOwnerId(userId);
    return saved;
  }

  @Transactional
  public Task update(long userId, long taskId, Task incoming) {
    Task existing =
        taskRepository.findByIdAndOwnerId(taskId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));

    existing.setTitle(incoming.getTitle());
    existing.setDueDate(incoming.getDueDate());
    existing.setEstimatedHours(incoming.getEstimatedHours());
    existing.setDifficulty(incoming.getDifficulty());
    existing.setPreferredStartTime(incoming.getPreferredStartTime());
    existing.setPreferredEndTime(incoming.getPreferredEndTime());
    existing.setMaxHoursPerDay(incoming.getMaxHoursPerDay());
    existing.setMinBlockHours(incoming.getMinBlockHours());
    existing.setMaxBlockHours(incoming.getMaxBlockHours());

    applyDefaultsAndValidate(existing);

    Task saved = taskRepository.save(existing);
    scheduleEntryRepository.deleteAllByOwnerId(userId);
    return saved;
  }

  @Transactional
  public void delete(long userId, long taskId) {
    Task task =
        taskRepository.findByIdAndOwnerId(taskId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));

    scheduleEntryRepository.deleteAllByTaskId(task.getId());
    taskRepository.delete(task);
  }

  @Transactional
  public void deleteAll(long userId) {
    scheduleEntryRepository.deleteAllByOwnerId(userId);
    taskRepository.deleteAllByOwnerId(userId);
  }

  private void applyDefaultsAndValidate(Task task) {
    if (task.getPreferredStartTime() == null) {
      task.setPreferredStartTime(DEFAULT_TASK_START);
    }
    if (task.getPreferredEndTime() == null) {
      task.setPreferredEndTime(DEFAULT_TASK_END);
    }
    if (task.getMaxHoursPerDay() == null) {
      task.setMaxHoursPerDay(DEFAULT_TASK_MAX_HOURS_PER_DAY);
    }
    if (task.getMinBlockHours() == null) {
      task.setMinBlockHours(DEFAULT_TASK_MIN_BLOCK_HOURS);
    }
    if (task.getMaxBlockHours() == null) {
      task.setMaxBlockHours(DEFAULT_TASK_MAX_BLOCK_HOURS);
    }

    if (!task.getPreferredEndTime().isAfter(task.getPreferredStartTime())) {
      throw new IllegalArgumentException(
          "Preferred end time must be after preferred start time");
    }
    if (task.getEstimatedHours() <= 0) {
      throw new IllegalArgumentException(
          "Estimated hours must be greater than 0");
    }
    if (task.getMaxHoursPerDay() <= 0) {
      throw new IllegalArgumentException(
          "Task max hours per day must be greater than 0");
    }
    if (task.getMinBlockHours() <= 0 || task.getMaxBlockHours() <= 0) {
      throw new IllegalArgumentException(
          "Task block hours must be greater than 0");
    }
    if (task.getMinBlockHours() > task.getMaxBlockHours()) {
      throw new IllegalArgumentException(
          "Task min block hours cannot be greater than max block hours");
    }
  }
}
