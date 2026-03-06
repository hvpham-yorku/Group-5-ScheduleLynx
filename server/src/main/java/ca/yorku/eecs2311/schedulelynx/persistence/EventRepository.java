package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.Event;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

  List<Event> findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(Long ownerId);

  List<Event> findAllByOwnerIdAndDateBetweenOrderByDateAscStartTimeAscIdAsc(
      Long ownerId, LocalDate startDate, LocalDate endDate);

  Optional<Event> findByIdAndOwnerId(Long id, Long ownerId);

  long deleteByIdAndOwnerId(Long id, Long ownerId);

  void deleteAllByOwnerId(Long ownerId);
}
