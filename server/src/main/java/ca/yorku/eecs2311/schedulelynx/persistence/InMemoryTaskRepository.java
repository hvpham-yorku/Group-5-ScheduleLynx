package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.Task;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("memory")
@Repository
public class InMemoryTaskRepository implements TaskRepository {

  private final Map<Long, List<Task>> byUser = new ConcurrentHashMap<>();
  private final AtomicLong nextId = new AtomicLong(1);

  @Override
  public Task save(long userId, Task task) {
    Task stored =
        new Task(nextId.getAndIncrement(), task.getTitle(), task.getDueDate(),
                 task.getEstimatedHours(), task.getDifficulty());
    byUser.computeIfAbsent(userId, k -> new ArrayList<>()).add(stored);
    return stored;
  }

  @Override
  public List<Task> findAll(long userId) {
    return new ArrayList<>(byUser.getOrDefault(userId, List.of()));
  }

  @Override
  public Optional<Task> findById(long userId, long id) {
    return byUser.getOrDefault(userId, List.of())
        .stream()
        .filter(t -> t.getId() != null && t.getId() == id)
        .findFirst();
  }

  @Override
  public Optional<Task> update(long userId, long id, Task updatedTask) {
    List<Task> tasks = byUser.get(userId);
    if (tasks == null)
      return Optional.empty();

    for (int i = 0; i < tasks.size(); i++) {
      Task existing = tasks.get(i);
      if (existing.getId() != null && existing.getId() == id) {
        Task stored = new Task(
            id, updatedTask.getTitle(), updatedTask.getDueDate(),
            updatedTask.getEstimatedHours(), updatedTask.getDifficulty());
        tasks.set(i, stored);
        return Optional.of(stored);
      }
    }
    return Optional.empty();
  }

  @Override
  public boolean delete(long userId, long id) {
    List<Task> tasks = byUser.get(userId);
    if (tasks == null)
      return false;
    return tasks.removeIf(t -> t.getId() != null && t.getId() == id);
  }
}
