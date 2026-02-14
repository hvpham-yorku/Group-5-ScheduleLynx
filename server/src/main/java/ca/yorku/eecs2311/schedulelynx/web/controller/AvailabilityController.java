package ca.yorku.eecs2311.schedulelynx.web.controller;

import ca.yorku.eecs2311.schedulelynx.domain.AvailabilityBlock;
import ca.yorku.eecs2311.schedulelynx.service.AvailabilityService;
import ca.yorku.eecs2311.schedulelynx.web.dto.AvailabilityRequest;
import ca.yorku.eecs2311.schedulelynx.web.dto.AvailabilityResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

  private final AvailabilityService service;

  public AvailabilityController(AvailabilityService service) {
    this.service = service;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AvailabilityResponse
  create(@Valid @RequestBody AvailabilityRequest request) {
    AvailabilityBlock created = service.create(new AvailabilityBlock(
        null, request.getDay(), request.getStart(), request.getEnd()));
    return toResponse(created);
  }

  @GetMapping
  public List<AvailabilityResponse> getAll() {
    return service.getAll().stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  public AvailabilityResponse getById(@PathVariable long id) {
    return service.getById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new AvailabilityNotFoundException(id));
  }

  @PutMapping("/{id}")
  public AvailabilityResponse
  update(@PathVariable long id,
         @Valid @RequestBody AvailabilityRequest request) {
    AvailabilityBlock updated = new AvailabilityBlock(
        null, request.getDay(), request.getStart(), request.getEnd());
    return service.update(id, updated)
        .map(this::toResponse)
        .orElseThrow(() -> new AvailabilityNotFoundException(id));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long id) {
    if (!service.delete(id)) {
      throw new AvailabilityNotFoundException(id);
    }
  }

  private AvailabilityResponse toResponse(AvailabilityBlock block) {
    return new AvailabilityResponse(block.getId(), block.getDay(),
                                    block.getStart(), block.getEnd());
  }
}
