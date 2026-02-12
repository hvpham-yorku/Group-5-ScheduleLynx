package ca.yorku.eecs2311.schedulelynx.web.controller;

import ca.yorku.eecs2311.schedulelynx.domain.ScheduledTaskBlock;
import ca.yorku.eecs2311.schedulelynx.service.ScheduleService;
import ca.yorku.eecs2311.schedulelynx.web.dto.ScheduleResponse;
import ca.yorku.eecs2311.schedulelynx.web.dto.ScheduledTaskBlockResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

  private final ScheduleService scheduleService;

  public ScheduleController(ScheduleService scheduleService) {
    this.scheduleService = scheduleService;
  }

  @GetMapping("/weekly")
  public ScheduleResponse generateWeekly() {
    ScheduleService.ScheduleResult result =
        scheduleService.generateWeeklyPlan();

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
