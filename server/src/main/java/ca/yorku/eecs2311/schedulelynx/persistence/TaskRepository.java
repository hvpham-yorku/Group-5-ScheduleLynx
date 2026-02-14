package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.Task;
import java.util.List;
import java.util.Optional;

public interface TaskRepository {

  Task save(Task task);

  List<Task> findAll();

  Optional<Task> findById(long id);

  Optional<Task> update(long id, Task updatedTask);

  boolean delete(long id);
}
