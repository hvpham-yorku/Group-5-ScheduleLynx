package ca.yorku.eecs2311.schedulelynx.server.persistence;

import ca.yorku.eecs2311.schedulelynx.server.domain.Event;
import java.util.List;
import java.util.Optional;

public interface EventRepository {
  Event save(long userId, Event event);

  List<Event> findAll(long userId);

  Optional<Event> findById(long userId, long id);

  Optional<Event> update(long userId, long id, Event updated);

  boolean delete(long userId, long id);
}
