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
  public Task save(Task task) {

    long id = nextId.getAndIncrement();

    Task stored =
        new Task(id, task.getTitle(), task.getDueDate(),
                 task.getEstimatedHours(), task.getDifficulty());

    tasks.put(id, stored);
    return stored;
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

  @Override
  public boolean delete(long id) {

    return tasks.remove(id) != null;
  }
}
