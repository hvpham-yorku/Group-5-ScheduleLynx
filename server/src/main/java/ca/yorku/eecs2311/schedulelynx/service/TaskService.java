package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.Task;
import ca.yorku.eecs2311.schedulelynx.persistence.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

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

  public Map<Long, Task> getAll() {

    return taskRepository.getAll();
  }

  public Optional<Task> getById(long id) {

    return taskRepository.getById(id);
  }

  public Optional<Task> update(long id, Task task) {

    validate(task);
    return taskRepository.update(id, task);
  }

  public void deleteAll() {

    taskRepository.deleteAll();
  }

  public boolean delete(long id) {

    return taskRepository.delete(id);
  }

  private void validate(Task task) {

    if (task == null) throw new IllegalArgumentException("Task must not be null");

    var title    = task.getTitle();
    var dueDate  = task.getDueDate();
    var estHours = task.getEstimatedHours();
    var estDiff  = task.getDifficulty();

    if (title == null)   throw new IllegalArgumentException("Task title must not be null");
    if (title.isBlank()) throw new IllegalArgumentException("Task title must not be blank");
    if (dueDate == null) throw new IllegalArgumentException("Task dueDate must not be null");
    if (estHours < 0)    throw new IllegalArgumentException("Task estimatedHours must not be negative");
    if (estDiff == null) throw new IllegalArgumentException("Task difficulty must not be null");

  }

}