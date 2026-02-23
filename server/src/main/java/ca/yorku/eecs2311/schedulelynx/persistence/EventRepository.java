package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.Event;
import java.util.List;
import java.util.Optional;

public interface EventRepository {

  Event save(Event event);

  List<Event> getAllEvents();

  Optional<Event> getEvent(long id);

  Optional<Event> update(long id, Event updated);

  boolean delete(long id);
}
