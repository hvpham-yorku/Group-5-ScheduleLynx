package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTaskRepository implements TaskRepository {

  private final List<Task> tasks = new ArrayList<>();
  private final AtomicLong nextId = new AtomicLong(1);

  @Override
  public Task save(Task task) {
    Task stored =
        new Task(nextId.getAndIncrement(), task.getTitle(), task.getDueDate(),
                 task.getEstimatedHours(), task.getDifficulty());

    tasks.add(stored);
    return stored;
  }

  @Override
  public List<Task> findAll() {
    return new ArrayList<>(tasks); // return a copy
  }

  @Override
  public Optional<Task> findById(long id) {
    return tasks.stream()
        .filter(t -> t.getId() != null && t.getId() == id)
        .findFirst();
  }

  @Override
  public Optional<Task> update(long id, Task updatedTask) {
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
  public boolean delete(long id) {
    return tasks.removeIf(t -> t.getId() != null && t.getId() == id);
  }
}
