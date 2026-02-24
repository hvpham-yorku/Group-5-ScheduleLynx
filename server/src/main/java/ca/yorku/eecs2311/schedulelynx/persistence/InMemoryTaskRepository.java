package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.Task;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryTaskRepository implements TaskRepository {

  private final Map<Long, Task> tasks = new HashMap<>();
  private final AtomicLong nextId = new AtomicLong(1);

  @Override
  public Task save(Task data) {

    long id = nextId.getAndIncrement();
    return putInRepo(id, data);
  }

  @Override
  public Optional<Task> update(long id, Task updatedTask) {


    Task existing = tasks.get(id);
    if (existing.getId() != null && existing.getId() == id) {

      Task stored = new Task(
              id, updatedTask.getTitle(), updatedTask.getDueDate(),
              updatedTask.getEstimatedHours(), updatedTask.getDifficulty());

      tasks.put(id, stored);
      return Optional.of(stored);
    }

    return Optional.empty();
  }

  private Task putInRepo(long id, Task data) {

    var title    = data.getTitle();
    var dueDate  = data.getDueDate();
    var estHours = data.getEstimatedHours();
    var diff     = data.getDifficulty();

    var task = new Task(id, title, dueDate, estHours, diff);

    tasks.put(id, task);
    return task;
  }

  @Override
  public Map<Long, Task> getAll() {

    return Map.copyOf(tasks);
  }

  @Override
  public Optional<Task> getById(long id) {

    return Optional.ofNullable(tasks.get(id));
  }

  @Override
  public void deleteAll() {

    tasks.clear();
  }

  @Override
  public boolean delete(long id) {

    return tasks.remove(id) != null;
  }
}
