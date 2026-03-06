package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.Task;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

  List<Task> findAllByOwnerIdOrderByDueDateAscIdAsc(Long ownerId);

  Optional<Task> findByIdAndOwnerId(Long id, Long ownerId);

  long deleteByIdAndOwnerId(Long id, Long ownerId);

  void deleteAllByOwnerId(Long ownerId);
}
