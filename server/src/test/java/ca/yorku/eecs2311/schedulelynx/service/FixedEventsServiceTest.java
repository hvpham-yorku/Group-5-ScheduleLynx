package ca.yorku.eecs2311.schedulelynx.service;

import static org.junit.jupiter.api.Assertions.*;

import ca.yorku.eecs2311.schedulelynx.domain.FixedEvent;
import ca.yorku.eecs2311.schedulelynx.domain.Weekday;
import ca.yorku.eecs2311.schedulelynx.persistence.InMemoryFixedEventRepository;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class FixedEventServiceTest {

  private FixedEventService newService() {
    return new FixedEventService(new InMemoryFixedEventRepository());
  }

  @Test
  void create_assignsId_andStoresEvent() {
    FixedEventService service = newService();

    FixedEvent created = service.create(
        new FixedEvent(null, "Lecture", Weekday.TUESDAY, LocalTime.of(10, 0),
                       LocalTime.of(11, 30)));

    assertNotNull(created.getId());
    assertEquals(1L, created.getId());
    assertEquals(1, service.getAll().size());
  }

  @Test
  void create_rejectsEmptyTitle() {
    FixedEventService service = newService();

    assertThrows(IllegalArgumentException.class,
                 ()
                     -> service.create(new FixedEvent(
                         null, "   ", Weekday.TUESDAY, LocalTime.of(10, 0),
                         LocalTime.of(11, 30))));
  }

  @Test
  void create_rejectsOverlapSameDay() {
    FixedEventService service = newService();

    service.create(new FixedEvent(null, "Lecture", Weekday.TUESDAY,
                                  LocalTime.of(10, 0), LocalTime.of(11, 30)));

    assertThrows(IllegalArgumentException.class,
                 ()
                     -> service.create(new FixedEvent(
                         null, "Lab", Weekday.TUESDAY, LocalTime.of(11, 0),
                         LocalTime.of(12, 0))));
  }
}
