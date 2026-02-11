package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.FixedEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryFixedEventRepository implements FixedEventRepository {

  private final List<FixedEvent> events = new ArrayList<>();
  private final AtomicLong nextId = new AtomicLong(1);

  @Override
  public FixedEvent save(FixedEvent event) {
    FixedEvent stored =
        new FixedEvent(nextId.getAndIncrement(), event.getTitle(),
                       event.getDay(), event.getStart(), event.getEnd());
    events.add(stored);
    return stored;
  }

  @Override
  public List<FixedEvent> findAll() {
    return new ArrayList<>(events);
  }

  @Override
  public Optional<FixedEvent> findById(long id) {
    return events.stream()
        .filter(e -> e.getId() != null && e.getId() == id)
        .findFirst();
  }

  @Override
  public Optional<FixedEvent> update(long id, FixedEvent updated) {
    for (int i = 0; i < events.size(); i++) {
      FixedEvent existing = events.get(i);
      if (existing.getId() != null && existing.getId() == id) {
        FixedEvent stored =
            new FixedEvent(id, updated.getTitle(), updated.getDay(),
                           updated.getStart(), updated.getEnd());
        events.set(i, stored);
        return Optional.of(stored);
      }
    }
    return Optional.empty();
  }

  @Override
  public boolean delete(long id) {
    return events.removeIf(e -> e.getId() != null && e.getId() == id);
  }
}
