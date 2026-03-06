package ca.yorku.eecs2311.schedulelynx.web.dto;

import java.util.List;

public record GenerateScheduleResponse(int totalEntriesCreated,
                                       int unscheduledTaskCount,
                                       List<String> warnings,
                                       List<ScheduleEntryResponse> entries) {}
