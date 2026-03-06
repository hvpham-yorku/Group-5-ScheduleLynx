package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.ScheduleEntry;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleEntryRepository
    extends JpaRepository<ScheduleEntry, Long> {

  List<ScheduleEntry>
  findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(Long ownerId);

  List<ScheduleEntry>
  findAllByOwnerIdAndDateBetweenOrderByDateAscStartTimeAscIdAsc(
      Long ownerId, LocalDate startDate, LocalDate endDate);

  void deleteAllByOwnerId(Long ownerId);
}
