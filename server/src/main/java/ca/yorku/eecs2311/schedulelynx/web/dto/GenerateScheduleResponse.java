package ca.yorku.eecs2311.schedulelynx.web.dto;

import java.util.List;

public record GenerateScheduleResponse(boolean feasible,
                                       boolean partiallyFeasible, String status,
                                       int totalEntriesCreated,
                                       int unscheduledTaskCount,
                                       List<String> warnings,
                                       List<ScheduleEntryResponse> entries) {}
