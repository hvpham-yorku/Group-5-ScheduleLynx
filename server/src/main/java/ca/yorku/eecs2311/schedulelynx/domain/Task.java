package ca.yorku.eecs2311.schedulelynx.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tasks")
public class Task {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

  @Column(nullable = false) private String title;

  @Column(nullable = false) private LocalDate dueDate;

  @Column(nullable = false) private int estimatedHours;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Difficulty difficulty;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User owner;

  public Task() {
    // JPA
  }

  public Task(Long id, String title, LocalDate dueDate, int estimatedHours,
              Difficulty difficulty) {
    this.id = id;
    this.title = title;
    this.dueDate = dueDate;
    this.estimatedHours = estimatedHours;
    this.difficulty = difficulty;
  }

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
}
