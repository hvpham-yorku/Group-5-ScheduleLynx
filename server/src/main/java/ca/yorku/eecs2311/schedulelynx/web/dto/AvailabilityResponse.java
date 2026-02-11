package ca.yorku.eecs2311.schedulelynx.web.dto;

import ca.yorku.eecs2311.schedulelynx.domain.Weekday;
import java.time.LocalTime;

public class AvailabilityResponse {

  private long id;
  private Weekday day;
  private LocalTime start;
  private LocalTime end;

  public AvailabilityResponse(long id, Weekday day, LocalTime start,
                              LocalTime end) {
    this.id = id;
    this.day = day;
    this.start = start;
    this.end = end;
  }

  public long getId() { return id; }

  public Weekday getDay() { return day; }

  public LocalTime getStart() { return start; }

  public LocalTime getEnd() { return end; }
}
