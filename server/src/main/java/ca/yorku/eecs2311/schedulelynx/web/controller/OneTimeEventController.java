package ca.yorku.eecs2311.schedulelynx.web.controller;

import ca.yorku.eecs2311.schedulelynx.domain.OneTimeEvent;
import ca.yorku.eecs2311.schedulelynx.service.OneTimeEventService;
import ca.yorku.eecs2311.schedulelynx.web.dto.OneTimeEventRequest;
import ca.yorku.eecs2311.schedulelynx.web.dto.OneTimeEventResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fixed-events")
public class OneTimeEventController {

  private final OneTimeEventService service;

  public OneTimeEventController(OneTimeEventService service) {
    this.service = service;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)

  public OneTimeEventResponse create(@Valid @RequestBody OneTimeEventRequest request) {

    var event = service.create(request);
    return toResponse(event);
  }

  @GetMapping
  public List<OneTimeEventResponse> getAll() {

    return service.getAll().stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  public OneTimeEventResponse getById(@PathVariable long id) {

    return service.getById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new OneTimeEventNotFoundException(id));
  }

  @PutMapping("/{id}")
  public OneTimeEventResponse update(
          @PathVariable long id,
          @Valid @RequestBody OneTimeEventRequest request)
  {
    OneTimeEvent updated =
        new OneTimeEvent(null, request.title(), request.day(),
                       request.start(), request.end());

    return service.update(id, updated)
        .map(this::toResponse)
        .orElseThrow(() -> new OneTimeEventNotFoundException(id));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long id) {

    if (!service.delete(id))
      throw new OneTimeEventNotFoundException(id);
  }

  private OneTimeEventResponse toResponse(OneTimeEvent event) {

    return new OneTimeEventResponse(event.getId(),
            event.getTitle(), event.getDay(),
            event.getStart(), event.getEnd());
  }
}
