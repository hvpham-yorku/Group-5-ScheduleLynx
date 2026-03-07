package ca.yorku.eecs2311.schedulelynx.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "schedule_entries")
public class ScheduleEntry {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  @Column(nullable = false) private LocalDate date;

  @Column(nullable = false) private LocalTime startTime;

  @Column(nullable = false) private LocalTime endTime;

  @Column(nullable = false) private int plannedHours;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "task_id", nullable = false)
  private Task task;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User owner;

  public ScheduleEntry() {
    // JPA
  }

  public ScheduleEntry(Long id, LocalDate date, LocalTime startTime,
                       LocalTime endTime, int plannedHours, Task task) {
    this.id = id;
    this.date = date;
    this.startTime = startTime;
    this.endTime = endTime;
    this.plannedHours = plannedHours;
    this.task = task;
  }

  public ScheduleEntry(LocalDate date, LocalTime startTime, LocalTime endTime,
                       int plannedHours, Task task, User owner) {
    this.date = date;
    this.startTime = startTime;
    this.endTime = endTime;
    this.plannedHours = plannedHours;
    this.task = task;
    this.owner = owner;
  }

  public Long getId() { return id; }

  public void setId(Long id) { this.id = id; }

  public LocalDate getDate() { return date; }

  public void setDate(LocalDate date) { this.date = date; }

  public LocalTime getStartTime() { return startTime; }

  public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

  public LocalTime getEndTime() { return endTime; }

  public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

  public int getPlannedHours() { return plannedHours; }

  public void setPlannedHours(int plannedHours) {
    this.plannedHours = plannedHours;
  }

  public Task getTask() { return task; }

  public void setTask(Task task) { this.task = task; }

  public User getOwner() { return owner; }

  public void setOwner(User owner) { this.owner = owner; }
}
