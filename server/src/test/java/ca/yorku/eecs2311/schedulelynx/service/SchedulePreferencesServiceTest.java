package ca.yorku.eecs2311.schedulelynx.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ca.yorku.eecs2311.schedulelynx.domain.User;
import ca.yorku.eecs2311.schedulelynx.domain.UserSchedulePreferences;
import ca.yorku.eecs2311.schedulelynx.persistence.ScheduleEntryRepository;
import ca.yorku.eecs2311.schedulelynx.persistence.UserRepository;
import ca.yorku.eecs2311.schedulelynx.persistence.UserSchedulePreferencesRepository;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SchedulePreferencesServiceTest {

  private UserSchedulePreferencesRepository preferencesRepository;
  private UserRepository userRepository;
  private ScheduleEntryRepository scheduleEntryRepository;
  private SchedulePreferencesService schedulePreferencesService;

  @BeforeEach
  void setUp() {
    preferencesRepository = mock(UserSchedulePreferencesRepository.class);
    userRepository = mock(UserRepository.class);
    scheduleEntryRepository = mock(ScheduleEntryRepository.class);
    schedulePreferencesService = new SchedulePreferencesService(
        preferencesRepository, userRepository, scheduleEntryRepository);
  }

  @Test
  void getOrCreate_shouldReturnExistingPreferences() {
    UserSchedulePreferences existing = new UserSchedulePreferences();
    ReflectionTestUtils.setField(existing, "id", 11L);

    when(preferencesRepository.findByOwnerId(1L))
        .thenReturn(Optional.of(existing));

    UserSchedulePreferences result = schedulePreferencesService.getOrCreate(1L);

    assertSame(existing, result);
    verify(preferencesRepository).findByOwnerId(1L);
    verify(userRepository, never()).findById(anyLong());
    verify(preferencesRepository, never()).save(any());
  }

  @Test
  void getOrCreate_shouldCreateDefaultPreferencesWhenMissing() {
    User user = testUser(1L, "user1");

    when(preferencesRepository.findByOwnerId(1L)).thenReturn(Optional.empty());
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(preferencesRepository.save(any(UserSchedulePreferences.class)))
        .thenAnswer(inv -> {
          UserSchedulePreferences prefs = inv.getArgument(0);
          ReflectionTestUtils.setField(prefs, "id", 100L);
          return prefs;
        });

    UserSchedulePreferences result = schedulePreferencesService.getOrCreate(1L);

    assertNotNull(result);
    assertEquals(100L, result.getId());
    assertEquals(user, result.getOwner());

    verify(preferencesRepository).findByOwnerId(1L);
    verify(userRepository).findById(1L);
    verify(preferencesRepository).save(any(UserSchedulePreferences.class));
  }

  @Test
  void getOrCreate_shouldThrowWhenUserMissing() {
    when(preferencesRepository.findByOwnerId(1L)).thenReturn(Optional.empty());
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class,
                 () -> schedulePreferencesService.getOrCreate(1L));

    verify(preferencesRepository).findByOwnerId(1L);
    verify(userRepository).findById(1L);
  }

  @Test
  void update_shouldModifyExistingPreferences() {
    UserSchedulePreferences existing = new UserSchedulePreferences();
    ReflectionTestUtils.setField(existing, "id", 11L);

    when(preferencesRepository.findByOwnerId(1L))
        .thenReturn(Optional.of(existing));
    when(preferencesRepository.save(any(UserSchedulePreferences.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    UserSchedulePreferences updated = schedulePreferencesService.update(
        1L, false, LocalTime.of(22, 30), LocalTime.of(7, 0));

    assertNotNull(updated);
    assertEquals(11L, updated.getId());
    assertEquals(false, updated.getAllowWeekendScheduling());
    assertEquals(LocalTime.of(22, 30), updated.getQuietHoursStart());
    assertEquals(LocalTime.of(7, 0), updated.getQuietHoursEnd());

    verify(preferencesRepository).findByOwnerId(1L);
    verify(preferencesRepository).save(existing);
    verify(scheduleEntryRepository).deleteAllByOwnerId(1L);
  }

  @Test
  void update_shouldCreatePreferencesIfMissing() {
    User user = testUser(1L, "user1");

    when(preferencesRepository.findByOwnerId(1L)).thenReturn(Optional.empty());
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(preferencesRepository.save(any(UserSchedulePreferences.class)))
        .thenAnswer(inv -> {
          UserSchedulePreferences prefs = inv.getArgument(0);
          if (prefs.getId() == null) {
            ReflectionTestUtils.setField(prefs, "id", 55L);
          }
          return prefs;
        });

    UserSchedulePreferences updated = schedulePreferencesService.update(
        1L, true, LocalTime.of(23, 0), LocalTime.of(8, 0));

    assertNotNull(updated);
    assertEquals(55L, updated.getId());
    assertEquals(true, updated.getAllowWeekendScheduling());
    assertEquals(LocalTime.of(23, 0), updated.getQuietHoursStart());
    assertEquals(LocalTime.of(8, 0), updated.getQuietHoursEnd());

    verify(preferencesRepository).findByOwnerId(1L);
    verify(userRepository).findById(1L);
    verify(preferencesRepository, atLeastOnce())
        .save(any(UserSchedulePreferences.class));
    verify(scheduleEntryRepository).deleteAllByOwnerId(1L);
  }

  @Test
  void update_shouldOverwritePreviousValues() {
    UserSchedulePreferences existing = new UserSchedulePreferences();
    ReflectionTestUtils.setField(existing, "id", 11L);
    existing.setAllowWeekendScheduling(true);
    existing.setQuietHoursStart(LocalTime.of(23, 0));
    existing.setQuietHoursEnd(LocalTime.of(8, 0));

    when(preferencesRepository.findByOwnerId(1L))
        .thenReturn(Optional.of(existing));
    when(preferencesRepository.save(any(UserSchedulePreferences.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    UserSchedulePreferences updated = schedulePreferencesService.update(
        1L, false, LocalTime.of(21, 0), LocalTime.of(6, 30));

    assertEquals(false, updated.getAllowWeekendScheduling());
    assertEquals(LocalTime.of(21, 0), updated.getQuietHoursStart());
    assertEquals(LocalTime.of(6, 30), updated.getQuietHoursEnd());
  }

  private User testUser(Long id, String username) {
    try {
      Constructor<User> ctor = User.class.getDeclaredConstructor();
      ctor.setAccessible(true);
      User u = ctor.newInstance();
      ReflectionTestUtils.setField(u, "id", id);
      ReflectionTestUtils.setField(u, "username", username);
      ReflectionTestUtils.setField(u, "email", username + "@example.com");
      ReflectionTestUtils.setField(u, "fullName", "Test User");
      ReflectionTestUtils.setField(u, "passwordHash", "hash");
      ReflectionTestUtils.setField(u, "createdAt", LocalDateTime.now());
      return u;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
