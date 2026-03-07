package ca.yorku.eecs2311.schedulelynx.web.controller;

import ca.yorku.eecs2311.schedulelynx.domain.Event;
import ca.yorku.eecs2311.schedulelynx.service.EventService;
import ca.yorku.eecs2311.schedulelynx.web.SessionUser;
import ca.yorku.eecs2311.schedulelynx.web.controller.errors.EventNotFoundException;
import ca.yorku.eecs2311.schedulelynx.web.dto.EventRequest;
import ca.yorku.eecs2311.schedulelynx.web.dto.EventResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public EventResponse create(@Valid @RequestBody EventRequest req,
                              HttpServletRequest request) {

    long userId = SessionUser.requireUserId(request);
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse create(@Valid @RequestBody EventRequest req) {

        var event = service.create(userId, req);
        return toResponse(event);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public EventResponse update(@Valid @RequestBody EventRequest req)
    {

        service.update(req);
        return getEvent(req.id());
    }

    @GetMapping
    public List<EventResponse> getAll(HttpServletRequest request) {

        var eventResList = new ArrayList<EventResponse>();
        long userId = SessionUser.requireUserId(request);

        for (var event : service.getAll(userId))
            eventResList.add(toResponse(event));

        return eventResList;
    }

    @GetMapping("/{id}")
    public EventResponse getEvent(@PathVariable long id,
                                  HttpServletRequest request) {

    long userId = SessionUser.requireUserId(request);

        var event = service.getEvent(userId, id);
        if (event.isEmpty()) throw new EventNotFoundException(id);
        return toResponse(event.get());
    }

    @DeleteMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll(HttpServletRequest request) {

    long userId = SessionUser.requireUserId(request);
    service.deleteAll(userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id, HttpServletRequest request) {

    long userId = SessionUser.requireUserId(request);

    if (!service.delete(userId, id))
        throw new EventNotFoundException(id);
    }

    private EventResponse toResponse(Event event) {

        var id    = event.getId();
        var title = event.getTitle();
        var day   = event.getDay();
        var start = event.getStart();
        var end   = event.getEnd();

        return new EventResponse(id, title, day, start, end);
    }

}
