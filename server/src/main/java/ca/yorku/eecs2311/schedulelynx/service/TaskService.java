package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.Task;
import ca.yorku.eecs2311.schedulelynx.domain.User;
import ca.yorku.eecs2311.schedulelynx.persistence.TaskRepository;
import ca.yorku.eecs2311.schedulelynx.persistence.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

  private final TaskRepository taskRepository;
  private final UserRepository userRepository;

  public TaskService(TaskRepository taskRepository,
                     UserRepository userRepository) {
    this.taskRepository = taskRepository;
    this.userRepository = userRepository;
  }

  public Task create(long userId, Task task) {
    validate(task);

    User owner = userRepository.findById(userId).orElseThrow(
        () -> new IllegalArgumentException("User not found"));

    task.setId(null);
    task.setOwner(owner);

    return taskRepository.save(task);
  }

  public List<Task> getAll(long userId) {
    return taskRepository.findAllByOwnerIdOrderByDueDateAscIdAsc(userId);
  }

  public Optional<Task> getById(long userId, long id) {
    return taskRepository.findByIdAndOwnerId(id, userId);
  }

  public Optional<Task> update(long userId, long id, Task updated) {
    validate(updated);

    return taskRepository.findByIdAndOwnerId(id, userId).map(existing -> {
      existing.setTitle(updated.getTitle());
      existing.setDueDate(updated.getDueDate());
      existing.setEstimatedHours(updated.getEstimatedHours());
      existing.setDifficulty(updated.getDifficulty());
      return taskRepository.save(existing);
    });
  }

  public boolean delete(long userId, long id) {
    return taskRepository.deleteByIdAndOwnerId(id, userId) > 0;
  }

  public void deleteAll(long userId) {
    taskRepository.deleteAllByOwnerId(userId);
  }

  private void validate(Task task) {
    if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
      throw new IllegalArgumentException("Title is required");
    }
    if (task.getDueDate() == null) {
      throw new IllegalArgumentException("Due date is required");
    }
    if (task.getEstimatedHours() <= 0) {
      throw new IllegalArgumentException(
          "Estimated hours must be greater than 0");
    }
    if (task.getDifficulty() == null) {
      throw new IllegalArgumentException("Difficulty is required");
    }
  }
}
