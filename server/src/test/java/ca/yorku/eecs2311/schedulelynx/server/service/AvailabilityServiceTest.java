package ca.yorku.eecs2311.schedulelynx.server.service;

import static org.junit.jupiter.api.Assertions.*;

import ca.yorku.eecs2311.schedulelynx.server.domain.AvailabilityBlock;
import ca.yorku.eecs2311.schedulelynx.server.domain.Weekday;
import ca.yorku.eecs2311.schedulelynx.server.persistence.InMemoryAvailabilityRepository;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AvailabilityServiceTest {

  private static final long USER_ID = 1L;

  private AvailabilityService service;

  @BeforeEach
  void setup() {
    service = new AvailabilityService(new InMemoryAvailabilityRepository());
  }

  @Test
  void create_and_getAll_work() {
    service.create(USER_ID, new AvailabilityBlock(null, Weekday.MONDAY,
                                                  LocalTime.of(9, 0),
                                                  LocalTime.of(12, 0)));

    var all = service.getAll(USER_ID);
    assertEquals(1, all.size());
    assertEquals(Weekday.MONDAY, all.get(0).getDay());
  }

  @Test
  void invalidTimeRange_throws() {
    var bad = new AvailabilityBlock(null, Weekday.MONDAY, LocalTime.of(12, 0),
                                    LocalTime.of(9, 0));

    assertThrows(IllegalArgumentException.class,
                 () -> service.create(USER_ID, bad));
  }

  @Test
  void update_and_delete_work() {
    var created =
        service.create(USER_ID, new AvailabilityBlock(null, Weekday.TUESDAY,
                                                      LocalTime.of(10, 0),
                                                      LocalTime.of(12, 0)));

    var updated = new AvailabilityBlock(
        null, Weekday.TUESDAY, LocalTime.of(11, 0), LocalTime.of(13, 0));

    var updatedOpt = service.update(USER_ID, created.getId(), updated);
    assertTrue(updatedOpt.isPresent());
    assertEquals(LocalTime.of(11, 0), updatedOpt.get().getStart());

    assertTrue(service.delete(USER_ID, created.getId()));
    assertTrue(service.getAll(USER_ID).isEmpty());
  }
}
