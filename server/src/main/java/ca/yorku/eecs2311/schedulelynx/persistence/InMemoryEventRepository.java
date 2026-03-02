package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.Event;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("memory")
@Repository
public class InMemoryEventRepository implements EventRepository {

  private final Map<Long, List<Event>> byUser = new ConcurrentHashMap<>();
  private final AtomicLong nextId = new AtomicLong(1);

  @Override
  public Event save(long userId, Event event) {
    Event stored = new Event(nextId.getAndIncrement(), event.getTitle(),
                             event.getDay(), event.getStart(), event.getEnd());
    byUser.computeIfAbsent(userId, k -> new ArrayList<>()).add(stored);
    return stored;
  }

  @Override
  public List<Event> findAll(long userId) {
    return new ArrayList<>(byUser.getOrDefault(userId, List.of()));
  }

  @Override
  public Optional<Event> findById(long userId, long id) {
    return byUser.getOrDefault(userId, List.of())
        .stream()
        .filter(e -> e.getId() != null && e.getId() == id)
        .findFirst();
  }

  @Override
  public Optional<Event> update(long userId, long id, Event updated) {
    List<Event> events = byUser.get(userId);
    if (events == null)
      return Optional.empty();

    for (int i = 0; i < events.size(); i++) {
      Event existing = events.get(i);
      if (existing.getId() != null && existing.getId() == id) {
        Event stored = new Event(id, updated.getTitle(), updated.getDay(),
                                 updated.getStart(), updated.getEnd());
        events.set(i, stored);
        return Optional.of(stored);
      }
    }
    return Optional.empty();
  }

  @Override
  public boolean delete(long userId, long id) {
    List<Event> events = byUser.get(userId);
    if (events == null)
      return false;
    return events.removeIf(e -> e.getId() != null && e.getId() == id);
  }
}
