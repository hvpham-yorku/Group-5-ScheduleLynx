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

        return service.getEvent(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    @PutMapping("/{id}")
    public EventResponse update(
            @PathVariable long id,
            @Valid @RequestBody EventRequest request)
    {
        Event updated =
                new Event(null, request.title(), request.day(),
                        request.start(), request.end());

        return service.update(id, updated)
                .map(this::toResponse)
                .orElseThrow(() -> new EventNotFoundException(id));
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
