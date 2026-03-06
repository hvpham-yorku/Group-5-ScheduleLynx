package ca.yorku.eecs2311.schedulelynx.web.dto;

import ca.yorku.eecs2311.schedulelynx.domain.RecurrenceType;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record EventResponse(Long id, String title, LocalDate date,
                            @JsonFormat(pattern = "HH:mm") LocalTime startTime,
                            @JsonFormat(pattern = "HH:mm") LocalTime endTime,
                            boolean recurring, RecurrenceType recurrenceType,
                            LocalDate recurrenceEnd,
                            Set<DayOfWeek> recurrenceDays) {}
