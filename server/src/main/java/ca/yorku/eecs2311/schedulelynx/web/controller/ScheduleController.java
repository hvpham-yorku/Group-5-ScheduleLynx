package ca.yorku.eecs2311.schedulelynx.web.controller;

import ca.yorku.eecs2311.schedulelynx.domain.ScheduleEntry;
import ca.yorku.eecs2311.schedulelynx.service.ScheduleService;
import ca.yorku.eecs2311.schedulelynx.web.SessionUser;
import ca.yorku.eecs2311.schedulelynx.web.dto.GenerateScheduleRequest;
import ca.yorku.eecs2311.schedulelynx.web.dto.GenerateScheduleResponse;
import ca.yorku.eecs2311.schedulelynx.web.dto.ScheduleEntryResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

  private final ScheduleService service;

  public ScheduleController(ScheduleService service) { this.service = service; }

  @GetMapping
  public List<ScheduleEntryResponse>
  getAll(@RequestParam(required = false) LocalDate startDate,
         @RequestParam(required = false) LocalDate endDate,
         HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);

    List<ScheduleEntry> entries;
    if (startDate != null && endDate != null) {
      entries = service.getBetween(userId, startDate, endDate);
    } else {
      entries = service.getAll(userId);
    }

    return entries.stream().map(this::toResponse).toList();
  }

  @PostMapping("/generate")
  @ResponseStatus(HttpStatus.CREATED)
  public GenerateScheduleResponse
  generate(@RequestBody(required = false) GenerateScheduleRequest req,
           HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);

    ScheduleService.ScheduleGenerationResult result =
        service.generate(userId, req);

    List<ScheduleEntryResponse> entries =
        result.entries().stream().map(this::toResponse).toList();

    return new GenerateScheduleResponse(
        entries.size(), result.warnings().size(), result.warnings(), entries);
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void clear(HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);
    service.clear(userId);
  }

  private ScheduleEntryResponse toResponse(ScheduleEntry entry) {
    return new ScheduleEntryResponse(
        entry.getId(), entry.getDate(), entry.getStartTime(),
        entry.getEndTime(), entry.getPlannedHours(), entry.getTask().getId(),
        entry.getTask().getTitle(), entry.getTask().getDueDate());
  }
}
