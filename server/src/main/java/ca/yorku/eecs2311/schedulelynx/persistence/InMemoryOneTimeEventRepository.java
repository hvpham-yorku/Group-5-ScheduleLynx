package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.OneTimeEvent;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryOneTimeEventRepository implements OneTimeEventRepository {

    private final Map<Long, OneTimeEvent> events = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public OneTimeEvent save(OneTimeEvent event) {

        long id = nextId.getAndIncrement();
        OneTimeEvent stored =
                new OneTimeEvent(id, event.getTitle(),
                        event.getDay(), event.getStart(), event.getEnd());
        events.put(id, stored);
        return stored;
    }

    @Override
    public List<OneTimeEvent> getAllEvents() {

        return List.copyOf(events.values());
    }

    @Override
    public Optional<OneTimeEvent> getEventByID(long id) {

        return Optional.ofNullable(events.get(id));
    }

    @Override
    public Optional<OneTimeEvent> update(long id, OneTimeEvent update) {

        var savedEvent = events.get(id);

        if (savedEvent == null) return Optional.empty();
        if (savedEvent.getId() == id)
            throw new RuntimeException("ID map key and event ID are different!");

        var title = update.getTitle();
        var day   = update.getDay();
        var start = update.getStart();
        var end   = update.getEnd();

        var updatedEvent = new OneTimeEvent(id, title, day, start, end);

        events.put(id, updatedEvent);
        return Optional.of(updatedEvent);
    }

    @Override
    public boolean delete(long id) {

        return events.remove(id) != null;
    }

}
