package ca.yorku.eecs2311.schedulelynx.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record SchedulePreferencesRequest(
    Boolean allowWeekendScheduling,
    @JsonFormat(pattern = "HH:mm") LocalTime quietHoursStart,
    @JsonFormat(pattern = "HH:mm") LocalTime quietHoursEnd) {}
