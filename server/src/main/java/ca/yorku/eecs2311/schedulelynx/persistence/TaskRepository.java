package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {

  Task save(Task task);

  Optional<Task> update(long id, Task updatedTask);

  List<Task> getAll();

  Optional<Task> getById(long id);

  boolean delete(long id);

}
