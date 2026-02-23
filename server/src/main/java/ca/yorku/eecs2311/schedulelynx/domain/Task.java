package ca.yorku.eecs2311.schedulelynx.domain;

import ca.yorku.eecs2311.schedulelynx.domain.events.Difficulty;

import java.time.LocalDate;

public class Task {

  private Long id;
  private String title;
  private LocalDate dueDate;
  private int estimatedHours;
  private Difficulty difficulty;

  public Task() {
    // default constructor for frameworks / serialization
  }

  public Task(Long id, String title, LocalDate dueDate, int estimatedHours,
              Difficulty difficulty) {
    this.id = id;
    this.title = title;
    this.dueDate = dueDate;
    this.estimatedHours = estimatedHours;
    this.difficulty = difficulty;
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
}
