package ca.yorku.eecs2311.schedulelynx.domain;

import java.time.LocalTime;

@Deprecated
public class TimeBlock {
  private Weekday day;
  private LocalTime start;
  private LocalTime end;

  public TimeBlock() {}

  public TimeBlock(Weekday day, LocalTime start, LocalTime end) {
    this.day = day;
    this.start = start;
    this.end = end;
  }

  public Weekday getDay() { return day; }

  public void setDay(Weekday day) { this.day = day; }

  public LocalTime getStart() { return start; }

  public void setStart(LocalTime start) { this.start = start; }

  public LocalTime getEnd() { return end; }

  public void setEnd(LocalTime end) { this.end = end; }
}
