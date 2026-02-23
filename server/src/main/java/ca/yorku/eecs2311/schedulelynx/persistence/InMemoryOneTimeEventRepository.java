package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.OneTimeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryOneTimeEventRepository implements OneTimeEventRepository {

  private final List<OneTimeEvent> events = new ArrayList<>();
  private final AtomicLong nextId = new AtomicLong(1);

  @Override
  public OneTimeEvent save(OneTimeEvent event) {
    
    OneTimeEvent stored =
        new OneTimeEvent(nextId.getAndIncrement(), event.getTitle(),
                       event.getDay(), event.getStart(), event.getEnd());
    events.add(stored);
    return stored;
  }

  @Override
  public List<OneTimeEvent> findAll() {

    return new ArrayList<>(events);
  }

  @Override
  public Optional<OneTimeEvent> findById(long id) {

    return events.stream()
        .filter(e -> e.getId() != null && e.getId() == id)
        .findFirst();
  }

  @Override
  public Optional<OneTimeEvent> update(long id, OneTimeEvent updated) {

    for (int i = 0; i < events.size(); i++) {
      OneTimeEvent existing = events.get(i);
      if (existing.getId() != null && existing.getId() == id) {
        OneTimeEvent stored =
            new OneTimeEvent(id, updated.getTitle(), updated.getDay(),
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
