package ca.yorku.eecs2311.schedulelynx.server.persistence;

import ca.yorku.eecs2311.schedulelynx.server.domain.Task;
import java.util.List;
import java.util.Optional;

public interface TaskRepository {
  Task save(long userId, Task task);

  List<Task> findAll(long userId);

  Optional<Task> findById(long userId, long id);

  Optional<Task> update(long userId, long id, Task updatedTask);

  boolean delete(long userId, long id);
}
