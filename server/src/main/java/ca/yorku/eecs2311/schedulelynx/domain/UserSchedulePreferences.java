package ca.yorku.eecs2311.schedulelynx.domain;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "user_schedule_preferences")
public class UserSchedulePreferences {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  @OneToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User owner;

  @Column(nullable = false) private Boolean allowWeekendScheduling = true;

  private LocalTime quietHoursStart;

  private LocalTime quietHoursEnd;

  public UserSchedulePreferences() {}

  public UserSchedulePreferences(User owner) {
    this.owner = owner;
    this.allowWeekendScheduling = true;
    this.quietHoursStart = LocalTime.of(23, 0);
    this.quietHoursEnd = LocalTime.of(8, 0);
  }

  public Long getId() { return id; }

  public void setId(Long id) { this.id = id; }

  public User getOwner() { return owner; }

  public void setOwner(User owner) { this.owner = owner; }

  public Boolean getAllowWeekendScheduling() { return allowWeekendScheduling; }

  public void setAllowWeekendScheduling(Boolean allowWeekendScheduling) {
    this.allowWeekendScheduling = allowWeekendScheduling;
  }

  public LocalTime getQuietHoursStart() { return quietHoursStart; }

  public void setQuietHoursStart(LocalTime quietHoursStart) {
    this.quietHoursStart = quietHoursStart;
  }

  public LocalTime getQuietHoursEnd() { return quietHoursEnd; }

  public void setQuietHoursEnd(LocalTime quietHoursEnd) {
    this.quietHoursEnd = quietHoursEnd;
  }
}
