package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.FixedEvent;
import java.util.List;
import java.util.Optional;

public interface FixedEventRepository {

  FixedEvent save(FixedEvent event);

  List<FixedEvent> findAll();

  Optional<FixedEvent> findById(long id);

  Optional<FixedEvent> update(long id, FixedEvent updated);

  boolean delete(long id);
}
