package ca.yorku.eecs2311.schedulelynx.web.dto;

import ca.yorku.eecs2311.schedulelynx.domain.Difficulty;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record
    TaskRequest(@NotBlank String title, @NotNull LocalDate dueDate,
                @Min(1) Integer estimatedHours, @NotNull Difficulty difficulty,
                @JsonFormat(pattern = "HH:mm") LocalTime preferredStartTime,
                @JsonFormat(pattern = "HH:mm") LocalTime preferredEndTime,
                @Min(1) Integer maxHoursPerDay, @Min(1) Integer minBlockHours,
                @Min(1) Integer maxBlockHours) {}
