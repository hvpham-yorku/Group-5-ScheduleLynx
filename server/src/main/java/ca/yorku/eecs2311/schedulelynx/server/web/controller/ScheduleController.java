package ca.yorku.eecs2311.schedulelynx.server.web.controller;

import ca.yorku.eecs2311.schedulelynx.server.domain.ScheduledTaskBlock;
import ca.yorku.eecs2311.schedulelynx.server.service.ScheduleService;
import ca.yorku.eecs2311.schedulelynx.server.web.SessionUser;
import ca.yorku.eecs2311.schedulelynx.server.web.dto.ScheduleResponse;
import ca.yorku.eecs2311.schedulelynx.server.web.dto.ScheduledTaskBlockResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

  private final ScheduleService scheduleService;

  public ScheduleController(ScheduleService scheduleService) {
    this.scheduleService = scheduleService;
  }

  @GetMapping("/weekly")
  public ScheduleResponse generateWeekly(@RequestParam String weekStart,
                                         HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);

    java.time.LocalDate start = java.time.LocalDate.parse(weekStart);

    ScheduleService.ScheduleResult result =
        scheduleService.generateWeeklyPlan(userId, start);

    List<ScheduledTaskBlockResponse> blocks =
        result.getBlocks().stream().map(this::toResponse).toList();

    return new ScheduleResponse(result.isFeasible(), result.getMessage(),
                                blocks);
  }

  private ScheduledTaskBlockResponse toResponse(ScheduledTaskBlock b) {
    return new ScheduledTaskBlockResponse(b.getDay(), b.getStart(), b.getEnd(),
                                          b.getTaskId(), b.getTaskTitle());
  }
}
