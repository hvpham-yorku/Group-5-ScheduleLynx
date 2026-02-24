package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.Task;

import java.util.Map;
import java.util.Optional;

public interface TaskRepository {

  Task save(Task data);

  Optional<Task> update(long id, Task updatedTask);

  Map<Long, Task> getAll();

  Optional<Task> getById(long id);

  void deleteAll();

  boolean delete(long id);

}
