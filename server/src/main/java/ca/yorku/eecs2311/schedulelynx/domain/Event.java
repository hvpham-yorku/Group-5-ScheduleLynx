package ca.yorku.eecs2311.schedulelynx.domain;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
public class Event {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private LocalDate date;

  @Column(nullable = false)
  private LocalTime startTime;

  @Column(nullable = false)
  private LocalTime endTime;

  @Column(nullable = false)
  private boolean recurring = false;

  @Enumerated(EnumType.STRING)
  @Column(name = "recurrence_type")
  private RecurrenceType recurrenceType;

  @Column(name = "recurrence_end")
  private LocalDate recurrenceEnd;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "event_recurrence_days", joinColumns = @JoinColumn(name = "event_id"))
  @Column(name = "day_of_week", nullable = false)
  @Enumerated(EnumType.STRING)
  private Set<DayOfWeek> recurrenceDays = new HashSet<>();

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User owner;

  public Event() {
    // JPA
  }

  public Event(Long id, String title, LocalDate date, LocalTime startTime, LocalTime endTime) {
    this.id = id;
    this.title = title;
    this.date = date;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public Event(
      Long id,
      String title,
      LocalDate date,
      LocalTime startTime,
      LocalTime endTime,
      boolean recurring,
      RecurrenceType recurrenceType,
      LocalDate recurrenceEnd,
      Set<DayOfWeek> recurrenceDays) {
    this.id = id;
    this.title = title;
    this.date = date;
    this.startTime = startTime;
    this.endTime = endTime;
    this.recurring = recurring;
    this.recurrenceType = recurrenceType;
    this.recurrenceEnd = recurrenceEnd;
    this.recurrenceDays = recurrenceDays != null ? new HashSet<>(recurrenceDays) : new HashSet<>();
  }

  public Event(
      String title,
      LocalDate date,
      LocalTime startTime,
      LocalTime endTime,
      boolean recurring,
      RecurrenceType recurrenceType,
      LocalDate recurrenceEnd,
      Set<DayOfWeek> recurrenceDays,
      User owner) {
    this.title = title;
    this.date = date;
    this.startTime = startTime;
    this.endTime = endTime;
    this.recurring = recurring;
    this.recurrenceType = recurrenceType;
    this.recurrenceEnd = recurrenceEnd;
    this.recurrenceDays = recurrenceDays != null ? new HashSet<>(recurrenceDays) : new HashSet<>();
    this.owner = owner;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public LocalTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalTime endTime) {
    this.endTime = endTime;
  }

  public boolean isRecurring() {
    return recurring;
  }

  public void setRecurring(boolean recurring) {
    this.recurring = recurring;
  }

  public RecurrenceType getRecurrenceType() {
    return recurrenceType;
  }

  public void setRecurrenceType(RecurrenceType recurrenceType) {
    this.recurrenceType = recurrenceType;
  }

  public LocalDate getRecurrenceEnd() {
    return recurrenceEnd;
  }

  public void setRecurrenceEnd(LocalDate recurrenceEnd) {
    this.recurrenceEnd = recurrenceEnd;
  }

  public Set<DayOfWeek> getRecurrenceDays() {
    return recurrenceDays;
  }

  public void setRecurrenceDays(Set<DayOfWeek> recurrenceDays) {
    this.recurrenceDays = recurrenceDays != null ? new HashSet<>(recurrenceDays) : new HashSet<>();
  }

  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }
}
