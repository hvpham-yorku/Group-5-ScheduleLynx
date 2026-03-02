package ca.yorku.eecs2311.schedulelynx.service;

import static org.junit.jupiter.api.Assertions.*;

import ca.yorku.eecs2311.schedulelynx.domain.Event;
import ca.yorku.eecs2311.schedulelynx.domain.Weekday;
import ca.yorku.eecs2311.schedulelynx.persistence.InMemoryEventRepository;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventServiceTest {

  private static final long USER_ID = 1L;

  private EventService service;

  @BeforeEach
  void setup() {
    service = new EventService(new InMemoryEventRepository());
  }

  @Test
  void create_and_getAll_work() {
    service.create(USER_ID,
                   new Event(null, "LEC", Weekday.MONDAY, LocalTime.of(10, 0),
                             LocalTime.of(11, 0)));

    var all = service.getAll(USER_ID);
    assertEquals(1, all.size());
    assertEquals("LEC", all.get(0).getTitle());
  }

  @Test
  void invalidTimeRange_throws() {
    var bad = new Event(null, "Bad", Weekday.MONDAY, LocalTime.of(12, 0),
                        LocalTime.of(11, 0));

    assertThrows(IllegalArgumentException.class,
                 () -> service.create(USER_ID, bad));
  }

  @Test
  void update_and_delete_work() {
    var created = service.create(
        USER_ID, new Event(null, "LAB", Weekday.TUESDAY, LocalTime.of(12, 0),
                           LocalTime.of(14, 0)));

    var updated = new Event(null, "LAB2", Weekday.TUESDAY, LocalTime.of(13, 0),
                            LocalTime.of(15, 0));

    var updatedOpt = service.update(USER_ID, created.getId(), updated);
    assertTrue(updatedOpt.isPresent());
    assertEquals("LAB2", updatedOpt.get().getTitle());

    assertTrue(service.delete(USER_ID, created.getId()));
    assertTrue(service.getAll(USER_ID).isEmpty());
  }
}
