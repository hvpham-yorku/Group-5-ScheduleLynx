package ca.yorku.eecs2311.schedulelynx.web.dto;

import ca.yorku.eecs2311.schedulelynx.domain.RecurrenceType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record
EventRequest(@NotBlank String title, @NotNull LocalDate date,
             @NotNull @JsonFormat(pattern = "HH:mm") LocalTime startTime,
             @NotNull @JsonFormat(pattern = "HH:mm") LocalTime endTime,
             boolean recurring, RecurrenceType recurrenceType,
             LocalDate recurrenceEnd, Set<DayOfWeek> recurrenceDays) {}
