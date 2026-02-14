package ca.yorku.eecs2311.schedulelynx.web.controller;

import ca.yorku.eecs2311.schedulelynx.domain.FixedEvent;
import ca.yorku.eecs2311.schedulelynx.service.FixedEventService;
import ca.yorku.eecs2311.schedulelynx.web.dto.FixedEventRequest;
import ca.yorku.eecs2311.schedulelynx.web.dto.FixedEventResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fixed-events")
public class FixedEventController {

  private final FixedEventService service;

  public FixedEventController(FixedEventService service) {
    this.service = service;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public FixedEventResponse
  create(@Valid @RequestBody FixedEventRequest request) {
    FixedEvent created = service.create(
        new FixedEvent(null, request.getTitle(), request.getDay(),
                       request.getStart(), request.getEnd()));
    return toResponse(created);
  }

  @GetMapping
  public List<FixedEventResponse> getAll() {
    return service.getAll().stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  public FixedEventResponse getById(@PathVariable long id) {
    return service.getById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new FixedEventNotFoundException(id));
  }

  @PutMapping("/{id}")
  public FixedEventResponse
  update(@PathVariable long id, @Valid @RequestBody FixedEventRequest request) {
    FixedEvent updated =
        new FixedEvent(null, request.getTitle(), request.getDay(),
                       request.getStart(), request.getEnd());
    return service.update(id, updated)
        .map(this::toResponse)
        .orElseThrow(() -> new FixedEventNotFoundException(id));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long id) {
    if (!service.delete(id)) {
      throw new FixedEventNotFoundException(id);
    }
  }

  private FixedEventResponse toResponse(FixedEvent event) {
    return new FixedEventResponse(event.getId(), event.getTitle(),
                                  event.getDay(), event.getStart(),
                                  event.getEnd());
  }
}
