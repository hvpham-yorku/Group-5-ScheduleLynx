package ca.yorku.eecs2311.schedulelynx.server.persistence;

import ca.yorku.eecs2311.schedulelynx.server.domain.AvailabilityBlock;
import java.util.List;
import java.util.Optional;

public interface AvailabilityRepository {
  AvailabilityBlock save(long userId, AvailabilityBlock block);

  List<AvailabilityBlock> findAll(long userId);

  Optional<AvailabilityBlock> findById(long userId, long id);

  Optional<AvailabilityBlock> update(long userId, long id,
                                     AvailabilityBlock updated);

  boolean delete(long userId, long id);
}
