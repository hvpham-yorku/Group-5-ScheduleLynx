package ca.yorku.eecs2311.schedulelynx.domain;

import java.time.LocalTime;

public class AvailabilityBlock extends TimeBlock {
  private Long id;

  public AvailabilityBlock() {}

  public AvailabilityBlock(Long id, Weekday day, LocalTime start,
                           LocalTime end) {
    super(day, start, end);
    this.id = id;
  }

  public Long getId() { return id; }

  public void setId(Long id) { this.id = id; }
}
