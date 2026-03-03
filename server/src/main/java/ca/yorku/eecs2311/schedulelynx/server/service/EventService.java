package ca.yorku.eecs2311.schedulelynx.server.service;

import ca.yorku.eecs2311.schedulelynx.server.domain.Event;
import ca.yorku.eecs2311.schedulelynx.server.persistence.EventRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class EventService {

  private final EventRepository repo;

  public EventService(EventRepository repo) { this.repo = repo; }

  public Event create(long userId, Event event) {
    validate(event);
    return repo.save(userId, event);
  }

  public List<Event> getAll(long userId) { return repo.findAll(userId); }

  public Optional<Event> getById(long userId, long id) {
    return repo.findById(userId, id);
  }

  public Optional<Event> update(long userId, long id, Event updated) {
    validate(updated);
    return repo.update(userId, id, updated);
  }

  public boolean delete(long userId, long id) {
    return repo.delete(userId, id);
  }

  private void validate(Event e) {
    if (e.getTitle() == null || e.getTitle().trim().isEmpty()) {
      throw new IllegalArgumentException("Title is required");
    }
    if (e.getDay() == null) {
      throw new IllegalArgumentException("Day is required");
    }
    LocalTime start = e.getStart();
    LocalTime end = e.getEnd();
    if (start == null || end == null || !start.isBefore(end)) {
      throw new IllegalArgumentException("Start time must be before end time");
    }
  }
}
