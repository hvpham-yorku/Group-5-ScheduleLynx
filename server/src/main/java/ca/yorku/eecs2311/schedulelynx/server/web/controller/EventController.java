package ca.yorku.eecs2311.schedulelynx.server.web.controller;

import ca.yorku.eecs2311.schedulelynx.server.domain.Event;
import ca.yorku.eecs2311.schedulelynx.server.service.EventService;
import ca.yorku.eecs2311.schedulelynx.server.web.SessionUser;
import ca.yorku.eecs2311.schedulelynx.server.web.controller.errors.EventNotFoundException;
import ca.yorku.eecs2311.schedulelynx.server.web.dto.EventRequest;
import ca.yorku.eecs2311.schedulelynx.server.web.dto.EventResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    Event created =
        service.create(userId, new Event(null, req.title(), req.day(),
                                         req.start(), req.end()));

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

    return service.getById(userId, id)
        .map(this::toResponse)
        .orElseThrow(() -> new EventNotFoundException(id));
  }

  @PutMapping("/{id}")
  public EventResponse update(@PathVariable long id,
                              @Valid @RequestBody EventRequest req,
                              HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);

    Event updated =
        new Event(null, req.title(), req.day(), req.start(), req.end());

    return service.update(userId, id, updated)
        .map(this::toResponse)
        .orElseThrow(() -> new EventNotFoundException(id));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long id, HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);

    if (!service.delete(userId, id)) {
      throw new EventNotFoundException(id);
    }
  }

  private EventResponse toResponse(Event e) {
    return new EventResponse(e.getId(), e.getTitle(), e.getDay(), e.getStart(),
                             e.getEnd());
  }
}
