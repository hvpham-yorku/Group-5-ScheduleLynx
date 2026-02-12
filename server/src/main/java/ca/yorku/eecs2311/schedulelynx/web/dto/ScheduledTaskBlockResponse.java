package ca.yorku.eecs2311.schedulelynx.web.dto;

import ca.yorku.eecs2311.schedulelynx.domain.Weekday;
import java.time.LocalTime;

public class ScheduledTaskBlockResponse {

  private Weekday day;
  private LocalTime start;
  private LocalTime end;
  private long taskId;
  private String taskTitle;

  public ScheduledTaskBlockResponse(Weekday day, LocalTime start, LocalTime end,
                                    long taskId, String taskTitle) {
    this.day = day;
    this.start = start;
    this.end = end;
    this.taskId = taskId;
    this.taskTitle = taskTitle;
  }

  public Weekday getDay() { return day; }

  public LocalTime getStart() { return start; }

  public LocalTime getEnd() { return end; }

  public long getTaskId() { return taskId; }

  public String getTaskTitle() { return taskTitle; }
}
