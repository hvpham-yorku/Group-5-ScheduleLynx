package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.Event;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryEventRepository implements EventRepository {

    private final Map<Long, Event> events = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Event save(Event event) {

        long id = nextId.getAndIncrement();
        return putInRepo(id, event);
    }

    @Override
    public Optional<Event> update(long id, Event update) {

        var savedEvent = events.get(id);

        if (savedEvent == null) return Optional.empty();
        if (savedEvent.getId() == id)
            throw new RuntimeException("ID map key and event ID are different!");

        var updatedEvent = putInRepo(id, update);
        return Optional.of(updatedEvent);
    }

    private Event putInRepo(long id, Event data) {

        var title = data.getTitle();
        var day   = data.getDay();
        var start = data.getStart();
        var end   = data.getEnd();

        var event = new Event(id, title, day, start, end);

        events.put(id, event);
        return event;
    }

    @Override
    public List<Event> getAllEvents() {

        return List.copyOf(events.values());
    }

    @Override
    public Optional<Event> getEventByID(long id) {

        return Optional.ofNullable(events.get(id));
    }

    @Override
    public boolean delete(long id) {

        return events.remove(id) != null;
    }

}
