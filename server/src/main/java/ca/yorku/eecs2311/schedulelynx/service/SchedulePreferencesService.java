package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.User;
import ca.yorku.eecs2311.schedulelynx.domain.UserSchedulePreferences;
import ca.yorku.eecs2311.schedulelynx.persistence.ScheduleEntryRepository;
import ca.yorku.eecs2311.schedulelynx.persistence.UserRepository;
import ca.yorku.eecs2311.schedulelynx.persistence.UserSchedulePreferencesRepository;
import java.time.LocalTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchedulePreferencesService {

  private final UserSchedulePreferencesRepository preferencesRepository;
  private final UserRepository userRepository;
  private final ScheduleEntryRepository scheduleEntryRepository;

  public SchedulePreferencesService(
      UserSchedulePreferencesRepository preferencesRepository,
      UserRepository userRepository,
      ScheduleEntryRepository scheduleEntryRepository) {
    this.preferencesRepository = preferencesRepository;
    this.userRepository = userRepository;
    this.scheduleEntryRepository = scheduleEntryRepository;
  }

  public UserSchedulePreferences getOrCreate(long userId) {
    return preferencesRepository.findByOwnerId(userId).orElseGet(() -> {
      User owner = userRepository.findById(userId).orElseThrow(
          () -> new IllegalArgumentException("User not found"));
      UserSchedulePreferences prefs = new UserSchedulePreferences(owner);
      return preferencesRepository.save(prefs);
    });
  }

  @Transactional
  public UserSchedulePreferences
  update(long userId, Boolean allowWeekendScheduling, LocalTime quietHoursStart,
         LocalTime quietHoursEnd) {
    UserSchedulePreferences prefs = getOrCreate(userId);

    prefs.setAllowWeekendScheduling(
        allowWeekendScheduling != null ? allowWeekendScheduling : true);
    prefs.setQuietHoursStart(quietHoursStart != null ? quietHoursStart
                                                     : LocalTime.of(23, 0));
    prefs.setQuietHoursEnd(quietHoursEnd != null ? quietHoursEnd
                                                 : LocalTime.of(8, 0));

    UserSchedulePreferences saved = preferencesRepository.save(prefs);
    scheduleEntryRepository.deleteAllByOwnerId(userId);
    return saved;
  }
}
