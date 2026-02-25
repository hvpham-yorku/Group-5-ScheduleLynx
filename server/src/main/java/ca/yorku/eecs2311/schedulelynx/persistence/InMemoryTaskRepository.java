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
  public Optional<Task> update(Task data) {

      var savedEvent = tasks.get(data.getId());
      if (savedEvent == null) return Optional.empty();

      var updatedEvent = putInRepo(data.getId(), data);
      return Optional.of(updatedEvent);
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
  public Optional<Task> getTask(long id) {

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
