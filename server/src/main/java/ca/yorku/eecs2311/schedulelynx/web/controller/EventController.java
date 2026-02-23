package ca.yorku.eecs2311.schedulelynx.web.controller;

import ca.yorku.eecs2311.schedulelynx.domain.Event;
import ca.yorku.eecs2311.schedulelynx.service.EventService;
import ca.yorku.eecs2311.schedulelynx.web.dto.EventRequest;
import ca.yorku.eecs2311.schedulelynx.web.dto.EventResponse;
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

    public EventResponse create(@Valid @RequestBody EventRequest request) {

        var event = service.create(request);
        return toResponse(event);
    }

    @PutMapping("/{id}")
    public EventResponse update(
            @PathVariable long id,
            @Valid @RequestBody EventRequest req)
    {
        var title = req.title();
        var day   = req.day();
        var start = req.start();
        var end   = req.end();

        var updateData = new Event(null, title, day, start, end);

        service.update(id, updateData);
        return getEvent(id);
    }

    @GetMapping
    public List<EventResponse> getAll() {

        var eventResList = new ArrayList<EventResponse>();
        for (var event : service.getAll())
            eventResList.add(toResponse(event));

        return eventResList;
    }

    @GetMapping("/{id}")
    public EventResponse getEvent(@PathVariable long id) {

        var event = service.getEvent(id);
        if (event.isEmpty()) throw new EventNotFoundException(id);
        return toResponse(event.get());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {

        if (!service.delete(id))
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
