package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.Task;
import ca.yorku.eecs2311.schedulelynx.persistence.TaskRepository;
import ca.yorku.eecs2311.schedulelynx.web.dto.TaskRequest;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class TaskService {

  private final TaskRepository repo;

  public TaskService(TaskRepository repo) {

    this.repo = repo;
  }

  public Task create(TaskRequest req) {

    var title    = req.title();
    var dueDate  = req.dueDate();
    var estHours = req.estimatedHours();
    var diff     = req.difficulty();

    var data = new Task(null, title, dueDate, estHours, diff);

    validate(data);
    return repo.save(data);
  }

  public Optional<Task> update(TaskRequest req) {

    var id       = req.id();
    var title    = req.title();
    var dueDate  = req.dueDate();
    var estHours = req.estimatedHours();
    var diff     = req.difficulty();

    var data = new Task(id, title, dueDate, estHours, diff);

    validate(data);
    return repo.update(data);
  }

  public Map<Long, Task> getAll() {

    return repo.getAll();
  }

  public Optional<Task> getTask(long id) {

    return repo.getTask(id);
  }

  public void deleteAll() {

    repo.deleteAll();
  }

  public boolean delete(long id) {

    return repo.delete(id);
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