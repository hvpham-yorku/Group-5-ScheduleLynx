package ca.yorku.eecs2311.schedulelynx.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleEntryResponse(
    Long id, LocalDate date, @JsonFormat(pattern = "HH:mm") LocalTime startTime,
    @JsonFormat(pattern = "HH:mm") LocalTime endTime, int plannedHours,
    Long taskId, String taskTitle, LocalDate taskDueDate) {}
