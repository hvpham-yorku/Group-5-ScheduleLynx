package ca.yorku.eecs2311.schedulelynx.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "tasks")
public class Task {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  @Column(nullable = false) private String title;

  @Column(nullable = false) private LocalDate dueDate;

  @Column(nullable = false) private int estimatedHours;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Difficulty difficulty = Difficulty.MEDIUM;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User owner;

  // Task-specific scheduling preferences
  private LocalTime preferredStartTime;

  private LocalTime preferredEndTime;

  private Integer maxHoursPerDay;

  private Integer minBlockHours;

  private Integer maxBlockHours;

  public Task() {}

  public Task(String title, LocalDate dueDate, int estimatedHours,
              Difficulty difficulty, User owner) {
    this.title = title;
    this.dueDate = dueDate;
    this.estimatedHours = estimatedHours;
    this.difficulty = difficulty;
    this.owner = owner;
  }

  public Long getId() { return id; }

  public void setId(Long id) { this.id = id; }

  public String getTitle() { return title; }

  public void setTitle(String title) { this.title = title; }

  public LocalDate getDueDate() { return dueDate; }

  public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

  public int getEstimatedHours() { return estimatedHours; }

  public void setEstimatedHours(int estimatedHours) {
    this.estimatedHours = estimatedHours;
  }

  public Difficulty getDifficulty() { return difficulty; }

  public void setDifficulty(Difficulty difficulty) {
    this.difficulty = difficulty;
  }

  public User getOwner() { return owner; }

  public void setOwner(User owner) { this.owner = owner; }

  public LocalTime getPreferredStartTime() { return preferredStartTime; }

  public void setPreferredStartTime(LocalTime preferredStartTime) {
    this.preferredStartTime = preferredStartTime;
  }

  public LocalTime getPreferredEndTime() { return preferredEndTime; }

  public void setPreferredEndTime(LocalTime preferredEndTime) {
    this.preferredEndTime = preferredEndTime;
  }

  public Integer getMaxHoursPerDay() { return maxHoursPerDay; }

  public void setMaxHoursPerDay(Integer maxHoursPerDay) {
    this.maxHoursPerDay = maxHoursPerDay;
  }

  public Integer getMinBlockHours() { return minBlockHours; }

  public void setMinBlockHours(Integer minBlockHours) {
    this.minBlockHours = minBlockHours;
  }

  public Integer getMaxBlockHours() { return maxBlockHours; }

  public void setMaxBlockHours(Integer maxBlockHours) {
    this.maxBlockHours = maxBlockHours;
  }
}
