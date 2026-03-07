package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.Task;
import ca.yorku.eecs2311.schedulelynx.persistence.TaskRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

  private final TaskRepository repo;

  public TaskService(TaskRepository repo) { this.repo = repo; }

  public Task create(long userId, Task task) {
    validate(task);
    return repo.save(userId, task);
  }

  public List<Task> getAll(long userId) {
    return repo.findAll(userId);
  }

  public Optional<Task> getById(long userId, long id) {
    return repo.findById(userId, id);
  }

  public Optional<Task> update(long userId, long id, Task updated) {
    validate(updated);
    return repo.update(userId, id, updated);
  }

  public boolean delete(long userId, long id) {
    return repo.delete(userId, id);
  }

  private void validate(Task task) {
    if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
      throw new IllegalArgumentException("Title is required");
    }
    if (task.getDueDate() == null) {
      throw new IllegalArgumentException("Due date is required");
    }
    if (task.getEstimatedHours() <= 0) {
      throw new IllegalArgumentException("Estimated hours must be > 0");
    }
    if (task.getDifficulty() == null) {
      throw new IllegalArgumentException("Difficulty is required");
    }
  }
}
