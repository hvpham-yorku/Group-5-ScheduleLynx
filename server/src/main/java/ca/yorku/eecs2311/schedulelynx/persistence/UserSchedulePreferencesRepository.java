package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.UserSchedulePreferences;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSchedulePreferencesRepository
    extends JpaRepository<UserSchedulePreferences, Long> {
  Optional<UserSchedulePreferences> findByOwnerId(long ownerId);
}
