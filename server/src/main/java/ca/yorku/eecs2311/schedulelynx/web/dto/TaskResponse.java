package ca.yorku.eecs2311.schedulelynx.web.dto;

import ca.yorku.eecs2311.schedulelynx.domain.Difficulty;
import java.time.LocalDate;

public class TaskResponse {

  private long id;
  private String title;
  private LocalDate dueDate;
  private int estimatedHours;
  private Difficulty difficulty;

  public TaskResponse(long id, String title, LocalDate dueDate,
                      int estimatedHours, Difficulty difficulty) {
    this.id = id;
    this.title = title;
    this.dueDate = dueDate;
    this.estimatedHours = estimatedHours;
    this.difficulty = difficulty;
  }

  public long getId() { return id; }

  public String getTitle() { return title; }

  public LocalDate getDueDate() { return dueDate; }

  public int getEstimatedHours() { return estimatedHours; }

  public Difficulty getDifficulty() { return difficulty; }
}
