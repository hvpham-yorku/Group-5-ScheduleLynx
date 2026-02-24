package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.Task;
import ca.yorku.eecs2311.schedulelynx.persistence.TaskRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

  private final TaskRepository taskRepository;

  public TaskService(TaskRepository taskRepository) {

    this.taskRepository = taskRepository;
  }

  public Task create(Task task) {

    validate(task);
    return taskRepository.save(task);
  }

  public List<Task> getAll() {

    return taskRepository.getAll();
  }

  public Optional<Task> getById(long id) {

    return taskRepository.getById(id);
  }

  public Optional<Task> update(long id, Task task) {

    validate(task);
    return taskRepository.update(id, task);
  }

  public boolean delete(long id) {

    return taskRepository.delete(id);
  }

  private void validate(Task task) {

    if (task == null) {
      throw new IllegalArgumentException("Task must not be null");
    }
    if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
      throw new IllegalArgumentException("Task title must not be empty");
    }
    if (task.getDueDate() == null) {
      throw new IllegalArgumentException("Task dueDate must not be null");
    }
    if (task.getEstimatedHours() <= 0) {
      throw new IllegalArgumentException("Task estimatedHours must be > 0");
    }
    if (task.getDifficulty() == null) {
      throw new IllegalArgumentException("Task difficulty must not be null");
    }
  }
}
