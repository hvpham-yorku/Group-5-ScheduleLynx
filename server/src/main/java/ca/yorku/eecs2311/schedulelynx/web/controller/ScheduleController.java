package ca.yorku.eecs2311.schedulelynx.web.controller;

import ca.yorku.eecs2311.schedulelynx.domain.ScheduleEntry;
import ca.yorku.eecs2311.schedulelynx.service.ScheduleService;
import ca.yorku.eecs2311.schedulelynx.web.SessionUser;
import ca.yorku.eecs2311.schedulelynx.web.dto.GenerateScheduleRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

  private final ScheduleService scheduleService;

  public ScheduleController(ScheduleService scheduleService) {
    this.scheduleService = scheduleService;
  }

  @GetMapping
  public List<ScheduleEntryResponse> getAll(HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);
    return scheduleService.getAll(userId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @GetMapping("/range")
  public List<ScheduleEntryResponse>
  getBetween(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
             LocalDate startDate,
             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
             LocalDate endDate, HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);
    return scheduleService.getBetween(userId, startDate, endDate)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @PostMapping("/generate")
  @ResponseStatus(HttpStatus.OK)
  public GenerateScheduleResponse
  generate(@RequestBody(required = false) GenerateScheduleRequest body,
           HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);
    ScheduleService.ScheduleGenerationResult result =
        scheduleService.generate(userId, body);

    List<ScheduleEntryResponse> entries =
        result.entries().stream().map(this::toResponse).toList();

    return new GenerateScheduleResponse(result.status().name(), entries,
                                        result.warnings());
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void clear(HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);
    scheduleService.clear(userId);
  }

  private ScheduleEntryResponse toResponse(ScheduleEntry entry) {
    return new ScheduleEntryResponse(
        entry.getId(), entry.getDate(), entry.getStartTime(),
        entry.getEndTime(), entry.getPlannedHours(), entry.getTask().getId(),
        entry.getTask().getTitle(), entry.getTask().getDueDate());
  }

  public record ScheduleEntryResponse(Long id, LocalDate date,
                                      java.time.LocalTime startTime,
                                      java.time.LocalTime endTime,
                                      Integer plannedHours, Long taskId,
                                      String taskTitle, LocalDate taskDueDate) {
  }

  public record GenerateScheduleResponse(String status,
                                         List<ScheduleEntryResponse> entries,
                                         List<String> warnings) {}
}
