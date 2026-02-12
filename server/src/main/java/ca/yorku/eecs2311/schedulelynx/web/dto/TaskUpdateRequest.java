package ca.yorku.eecs2311.schedulelynx.web.dto;

import ca.yorku.eecs2311.schedulelynx.domain.Difficulty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class TaskUpdateRequest {

  @NotBlank private String title;

  @NotNull private LocalDate dueDate;

  @Min(1) private int estimatedHours;

  @NotNull private Difficulty difficulty;

  public TaskUpdateRequest() {}

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
