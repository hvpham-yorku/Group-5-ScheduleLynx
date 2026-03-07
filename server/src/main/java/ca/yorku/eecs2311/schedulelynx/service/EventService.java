package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.Event;
import ca.yorku.eecs2311.schedulelynx.domain.RecurrenceType;
import ca.yorku.eecs2311.schedulelynx.domain.User;
import ca.yorku.eecs2311.schedulelynx.persistence.EventRepository;
import ca.yorku.eecs2311.schedulelynx.persistence.ScheduleEntryRepository;
import ca.yorku.eecs2311.schedulelynx.persistence.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

  private final EventRepository eventRepository;
  private final UserRepository userRepository;
  private final ScheduleEntryRepository scheduleEntryRepository;

  public EventService(EventRepository eventRepository,
                      UserRepository userRepository,
                      ScheduleEntryRepository scheduleEntryRepository) {
    this.eventRepository = eventRepository;
    this.userRepository = userRepository;
    this.scheduleEntryRepository = scheduleEntryRepository;
  }

  @Transactional
  public Event create(long userId, Event event) {
    validate(event);

    User owner = userRepository.findById(userId).orElseThrow(
        () -> new IllegalArgumentException("User not found"));

    event.setId(null);
    event.setOwner(owner);

    Event saved = eventRepository.save(event);

    // Any event change makes old generated schedule stale
    scheduleEntryRepository.deleteAllByOwnerId(userId);

    return saved;
  }

  public List<Event> getAll(long userId) {
    return eventRepository.findAllByOwnerIdOrderByDateAscStartTimeAscIdAsc(
        userId);
  }

  public Optional<Event> getById(long userId, long id) {
    return eventRepository.findByIdAndOwnerId(id, userId);
  }

  @Transactional
  public Optional<Event> update(long userId, long id, Event updated) {
    validate(updated);

    return eventRepository.findByIdAndOwnerId(id, userId).map(existing -> {
      existing.setTitle(updated.getTitle());
      existing.setDate(updated.getDate());
      existing.setStartTime(updated.getStartTime());
      existing.setEndTime(updated.getEndTime());
      existing.setRecurring(updated.isRecurring());
      existing.setRecurrenceType(updated.getRecurrenceType());
      existing.setRecurrenceEnd(updated.getRecurrenceEnd());
      existing.setRecurrenceDays(updated.getRecurrenceDays());

      Event saved = eventRepository.save(existing);

      // Any event change makes old generated schedule stale
      scheduleEntryRepository.deleteAllByOwnerId(userId);

      return saved;
    });
  }

  @Transactional
  public boolean delete(long userId, long id) {
    boolean deleted = eventRepository.deleteByIdAndOwnerId(id, userId) > 0;

    if (deleted) {
      // Any event change makes old generated schedule stale
      scheduleEntryRepository.deleteAllByOwnerId(userId);
    }

    return deleted;
  }

  @Transactional
  public void deleteAll(long userId) {
    eventRepository.deleteAllByOwnerId(userId);

    // Any event change makes old generated schedule stale
    scheduleEntryRepository.deleteAllByOwnerId(userId);
  }

  private void validate(Event event) {
    if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
      throw new IllegalArgumentException("Title is required");
    }
    if (event.getDate() == null) {
      throw new IllegalArgumentException("Date is required");
    }
    if (event.getStartTime() == null) {
      throw new IllegalArgumentException("Start time is required");
    }
    if (event.getEndTime() == null) {
      throw new IllegalArgumentException("End time is required");
    }
    if (!event.getEndTime().isAfter(event.getStartTime())) {
      throw new IllegalArgumentException("End time must be after start time");
    }

    if (event.isRecurring()) {
      if (event.getRecurrenceType() == null) {
        throw new IllegalArgumentException(
            "Recurrence type is required for recurring events");
      }
      if (event.getRecurrenceEnd() != null &&
          event.getRecurrenceEnd().isBefore(event.getDate())) {
        throw new IllegalArgumentException(
            "Recurrence end cannot be before event date");
      }

      if ((event.getRecurrenceType() == RecurrenceType.WEEKLY ||
           event.getRecurrenceType() == RecurrenceType.BIWEEKLY) &&
          (event.getRecurrenceDays() == null ||
           event.getRecurrenceDays().isEmpty())) {
        throw new IllegalArgumentException(
            "Recurrence days are required for weekly/biweekly events");
      }
    } else {
      event.setRecurrenceType(null);
      event.setRecurrenceEnd(null);
      event.setRecurrenceDays(null);
    }
  }
}
