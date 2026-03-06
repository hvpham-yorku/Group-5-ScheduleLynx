package ca.yorku.eecs2311.schedulelynx.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;

public record
GenerateScheduleRequest(LocalDate startDate,
                        @JsonFormat(pattern = "HH:mm") LocalTime dayStartTime,
                        @JsonFormat(pattern = "HH:mm") LocalTime dayEndTime,
                        Integer maxHoursPerDay, Integer maxBlockHours) {}
