package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.OneTimeEvent;
import java.util.List;
import java.util.Optional;

public interface OneTimeEventRepository {

  OneTimeEvent save(OneTimeEvent event);

  List<OneTimeEvent> findAll();

  Optional<OneTimeEvent> findById(long id);

  Optional<OneTimeEvent> update(long id, OneTimeEvent updated);

  boolean delete(long id);
}
