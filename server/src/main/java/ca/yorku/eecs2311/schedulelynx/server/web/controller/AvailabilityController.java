package ca.yorku.eecs2311.schedulelynx.server.web.controller;

import ca.yorku.eecs2311.schedulelynx.server.domain.AvailabilityBlock;
import ca.yorku.eecs2311.schedulelynx.server.service.AvailabilityService;
import ca.yorku.eecs2311.schedulelynx.server.web.SessionUser;
import ca.yorku.eecs2311.schedulelynx.server.web.controller.errors.AvailabilityNotFoundException;
import ca.yorku.eecs2311.schedulelynx.server.web.dto.AvailabilityRequest;
import ca.yorku.eecs2311.schedulelynx.server.web.dto.AvailabilityResponse;
import jakarta.servlet.http.HttpServletRequest;
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
  create(@Valid @RequestBody AvailabilityRequest req,
         HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);

    AvailabilityBlock created = service.create(
        userId, new AvailabilityBlock(null, req.getDay(), req.getStart(),
                                      req.getEnd()));
    return toResponse(created);
  }

  @GetMapping
  public List<AvailabilityResponse> getAll(HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);
    return service.getAll(userId).stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  public AvailabilityResponse getById(@PathVariable long id,
                                      HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);

    return service.getById(userId, id)
        .map(this::toResponse)
        .orElseThrow(() -> new AvailabilityNotFoundException(id));
  }

  @PutMapping("/{id}")
  public AvailabilityResponse
  update(@PathVariable long id, @Valid @RequestBody AvailabilityRequest req,
         HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);

    AvailabilityBlock updated =
        new AvailabilityBlock(null, req.getDay(), req.getStart(), req.getEnd());
    return service.update(userId, id, updated)
        .map(this::toResponse)
        .orElseThrow(() -> new AvailabilityNotFoundException(id));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long id, HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);

    if (!service.delete(userId, id)) {
      throw new AvailabilityNotFoundException(id);
    }
  }

  private AvailabilityResponse toResponse(AvailabilityBlock b) {
    return new AvailabilityResponse(b.getId(), b.getDay(), b.getStart(),
                                    b.getEnd());
  }
}
