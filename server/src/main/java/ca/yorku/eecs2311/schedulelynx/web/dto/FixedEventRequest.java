package ca.yorku.eecs2311.schedulelynx.web.dto;

import ca.yorku.eecs2311.schedulelynx.domain.Weekday;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

@Deprecated
public class FixedEventRequest {

  @NotBlank private String title;

  @NotNull private Weekday day;

  @NotNull private LocalTime start;

  @NotNull private LocalTime end;

  public FixedEventRequest() {}

  public String getTitle() { return title; }

  public void setTitle(String title) { this.title = title; }

  public Weekday getDay() { return day; }

  public void setDay(Weekday day) { this.day = day; }

  public LocalTime getStart() { return start; }

  public void setStart(LocalTime start) { this.start = start; }

  public LocalTime getEnd() { return end; }

  public void setEnd(LocalTime end) { this.end = end; }
}
