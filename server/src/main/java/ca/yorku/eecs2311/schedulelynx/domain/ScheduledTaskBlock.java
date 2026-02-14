package ca.yorku.eecs2311.schedulelynx.domain;

import java.time.LocalTime;

public class ScheduledTaskBlock {

  private Weekday day;
  private LocalTime start;
  private LocalTime end;

  private long taskId;
  private String taskTitle;

  public ScheduledTaskBlock() {}

  public ScheduledTaskBlock(Weekday day, LocalTime start, LocalTime end,
                            long taskId, String taskTitle) {
    this.day = day;
    this.start = start;
    this.end = end;
    this.taskId = taskId;
    this.taskTitle = taskTitle;
  }

  public Weekday getDay() { return day; }

  public void setDay(Weekday day) { this.day = day; }

  public LocalTime getStart() { return start; }

  public void setStart(LocalTime start) { this.start = start; }

  public LocalTime getEnd() { return end; }

  public void setEnd(LocalTime end) { this.end = end; }

  public long getTaskId() { return taskId; }

  public void setTaskId(long taskId) { this.taskId = taskId; }

  public String getTaskTitle() { return taskTitle; }

  public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }
}
