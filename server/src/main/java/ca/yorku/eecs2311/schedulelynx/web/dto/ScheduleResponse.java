package ca.yorku.eecs2311.schedulelynx.web.dto;

import java.util.List;

public class ScheduleResponse {

  private boolean feasible;
  private String message;
  private List<ScheduledTaskBlockResponse> blocks;

  public ScheduleResponse(boolean feasible, String message,
                          List<ScheduledTaskBlockResponse> blocks) {
    this.feasible = feasible;
    this.message = message;
    this.blocks = blocks;
  }

  public boolean isFeasible() { return feasible; }

  public String getMessage() { return message; }

  public List<ScheduledTaskBlockResponse> getBlocks() { return blocks; }
}
