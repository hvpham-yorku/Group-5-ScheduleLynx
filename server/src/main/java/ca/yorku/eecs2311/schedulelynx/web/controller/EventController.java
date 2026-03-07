package ca.yorku.eecs2311.schedulelynx.web.controller;

import ca.yorku.eecs2311.schedulelynx.domain.Event;
import ca.yorku.eecs2311.schedulelynx.service.EventService;
import ca.yorku.eecs2311.schedulelynx.web.SessionUser;
import ca.yorku.eecs2311.schedulelynx.web.dto.EventRequest;
import ca.yorku.eecs2311.schedulelynx.web.dto.EventResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/events")
public class EventController {

  private final EventService service;

  public EventController(EventService service) { this.service = service; }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public EventResponse create(@Valid @RequestBody EventRequest req,
                              HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);

    Event created = service.create(
        userId, new Event(null, req.title(), req.date(), req.startTime(),
                          req.endTime(), req.recurring(), req.recurrenceType(),
                          req.recurrenceEnd(), req.recurrenceDays()));

    return toResponse(created);
  }

  @GetMapping
  public List<EventResponse> getAll(HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);
    return service.getAll(userId).stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  public EventResponse getById(@PathVariable long id,
                               HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);

    Event event =
        service.getById(userId, id)
            .orElseThrow(()
                             -> new ResponseStatusException(
                                 HttpStatus.NOT_FOUND, "Event not found"));

    return toResponse(event);
  }

  @PutMapping("/{id}")
  public EventResponse update(@PathVariable long id,
                              @Valid @RequestBody EventRequest req,
                              HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);

    Event updated =
        new Event(null, req.title(), req.date(), req.startTime(), req.endTime(),
                  req.recurring(), req.recurrenceType(), req.recurrenceEnd(),
                  req.recurrenceDays());

    Event saved =
        service.update(userId, id, updated)
            .orElseThrow(()
                             -> new ResponseStatusException(
                                 HttpStatus.NOT_FOUND, "Event not found"));

    return toResponse(saved);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long id, HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);

    if (!service.delete(userId, id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                        "Event not found");
    }
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAll(HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);
    service.deleteAll(userId);
  }

  private EventResponse toResponse(Event event) {
    return new EventResponse(
        event.getId(), event.getTitle(), event.getDate(), event.getStartTime(),
        event.getEndTime(), event.isRecurring(), event.getRecurrenceType(),
        event.getRecurrenceEnd(), event.getRecurrenceDays());
  }
}
