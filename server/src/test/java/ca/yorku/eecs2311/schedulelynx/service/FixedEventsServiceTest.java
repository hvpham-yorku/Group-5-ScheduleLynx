package ca.yorku.eecs2311.schedulelynx.service;

import static org.junit.jupiter.api.Assertions.*;

import ca.yorku.eecs2311.schedulelynx.domain.OneTimeEvent;
import ca.yorku.eecs2311.schedulelynx.domain.Weekday;
import ca.yorku.eecs2311.schedulelynx.persistence.InMemoryOneTimeEventRepository;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class OneTimeEventServiceTest {

  private OneTimeEventService newService() {
    return new OneTimeEventService(new InMemoryOneTimeEventRepository());
  }

  @Test
  void create_assignsId_andStoresEvent() {
    OneTimeEventService service = newService();

    OneTimeEvent created = service.create(
        new OneTimeEvent(null, "Lecture", Weekday.TUESDAY, LocalTime.of(10, 0),
                       LocalTime.of(11, 30)));

    assertNotNull(created.getId());
    assertEquals(1L, created.getId());
    assertEquals(1, service.getAll().size());
  }

  @Test
  void create_rejectsEmptyTitle() {
    OneTimeEventService service = newService();

    assertThrows(IllegalArgumentException.class,
                 ()
                     -> service.create(new OneTimeEvent(
                         null, "   ", Weekday.TUESDAY, LocalTime.of(10, 0),
                         LocalTime.of(11, 30))));
  }

  @Test
  void create_rejectsOverlapSameDay() {
    OneTimeEventService service = newService();

    service.create(new OneTimeEvent(null, "Lecture", Weekday.TUESDAY,
                                  LocalTime.of(10, 0), LocalTime.of(11, 30)));

    assertThrows(IllegalArgumentException.class,
                 ()
                     -> service.create(new OneTimeEvent(
                         null, "Lab", Weekday.TUESDAY, LocalTime.of(11, 0),
                         LocalTime.of(12, 0))));
  }
}
