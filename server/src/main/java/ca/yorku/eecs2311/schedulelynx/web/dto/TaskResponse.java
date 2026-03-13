package ca.yorku.eecs2311.schedulelynx.web.dto;

import ca.yorku.eecs2311.schedulelynx.domain.Difficulty;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;

public record
    TaskResponse(Long id, String title, LocalDate dueDate,
                 Integer estimatedHours, Difficulty difficulty,
                 @JsonFormat(pattern = "HH:mm") LocalTime preferredStartTime,
                 @JsonFormat(pattern = "HH:mm") LocalTime preferredEndTime,
                 Integer maxHoursPerDay, Integer minBlockHours,
                 Integer maxBlockHours) {}
