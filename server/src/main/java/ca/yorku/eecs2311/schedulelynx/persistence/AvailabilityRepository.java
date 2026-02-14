package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.AvailabilityBlock;
import java.util.List;
import java.util.Optional;

public interface AvailabilityRepository {

  AvailabilityBlock save(AvailabilityBlock block);

  List<AvailabilityBlock> findAll();

  Optional<AvailabilityBlock> findById(long id);

  Optional<AvailabilityBlock> update(long id, AvailabilityBlock updated);

  boolean delete(long id);
}
