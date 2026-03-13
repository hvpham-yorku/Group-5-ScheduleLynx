package ca.yorku.eecs2311.schedulelynx.web.controller;

import ca.yorku.eecs2311.schedulelynx.domain.UserSchedulePreferences;
import ca.yorku.eecs2311.schedulelynx.service.SchedulePreferencesService;
import ca.yorku.eecs2311.schedulelynx.web.SessionUser;
import ca.yorku.eecs2311.schedulelynx.web.dto.SchedulePreferencesRequest;
import ca.yorku.eecs2311.schedulelynx.web.dto.SchedulePreferencesResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preferences/schedule")
public class SchedulePreferencesController {

  private final SchedulePreferencesService schedulePreferencesService;

  public SchedulePreferencesController(
      SchedulePreferencesService schedulePreferencesService) {
    this.schedulePreferencesService = schedulePreferencesService;
  }

  @GetMapping
  public SchedulePreferencesResponse get(HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);
    UserSchedulePreferences prefs =
        schedulePreferencesService.getOrCreate(userId);
    return toResponse(prefs);
  }

  @PutMapping
  public SchedulePreferencesResponse
  update(@RequestBody SchedulePreferencesRequest body,
         HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);
    UserSchedulePreferences prefs = schedulePreferencesService.update(
        userId, body.allowWeekendScheduling(), body.quietHoursStart(),
        body.quietHoursEnd());
    return toResponse(prefs);
  }

  private SchedulePreferencesResponse
  toResponse(UserSchedulePreferences prefs) {
    return new SchedulePreferencesResponse(prefs.getAllowWeekendScheduling(),
                                           prefs.getQuietHoursStart(),
                                           prefs.getQuietHoursEnd());
  }
}
