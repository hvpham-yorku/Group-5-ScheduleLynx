package ca.yorku.eecs2311.schedulelynx.web.dto;

import ca.yorku.eecs2311.schedulelynx.domain.Weekday;
import java.time.LocalTime;

@Deprecated
public class FixedEventResponse {

  private long id;
  private String title;
  private Weekday day;
  private LocalTime start;
  private LocalTime end;

  public FixedEventResponse(long id, String title, Weekday day, LocalTime start,
                            LocalTime end) {
    this.id = id;
    this.title = title;
    this.day = day;
    this.start = start;
    this.end = end;
  }

  public long getId() { return id; }

  public String getTitle() { return title; }

  public Weekday getDay() { return day; }

  public LocalTime getStart() { return start; }

  public LocalTime getEnd() { return end; }
}
