package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.Weekday;
import ca.yorku.eecs2311.schedulelynx.persistence.InMemoryEventRepository;
import ca.yorku.eecs2311.schedulelynx.web.dto.EventRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class EventServiceTest {

  private EventService newService() {
    return new EventService(new InMemoryEventRepository());
  }

  @Test
  void create_assignsId_andStoresEvent() {

    var service = newService();

    var title = "Lecture";
    var day   = Weekday.TUESDAY;
    var start = LocalTime.of(10, 0);
    var end   = LocalTime.of(11, 30);

    var request = new EventRequest(title, day, start,end);
    var event = service.create(request);

    assertNotNull(event.getId());
    assertEquals(1L, event.getId());
    assertEquals(1, service.getAll().size());
  }

  @Test
  void create_rejectsEmptyTitle() {

    var service = newService();

    var title = "   ";
    var day   = Weekday.TUESDAY;
    var start = LocalTime.of(10, 0);
    var end   = LocalTime.of(11, 30);

    var request = new EventRequest(title, day, start,end);

    assertThrows(IllegalArgumentException.class, () -> service.create(request));
  }

  @Test
  void create_rejectsOverlapSameDay() {

    var service = newService();

    var title = "Lecture";
    var day   = Weekday.TUESDAY;
    var start = LocalTime.of(10, 0);
    var end   = LocalTime.of(11, 30);

    var request1 = new EventRequest(title, day, start,end);
    var event = service.create(request1);

    title = "Lab"; // LIKE THE DOG!? but I like cats
    day   = Weekday.TUESDAY;
    start = LocalTime.of(11, 0);
    end   = LocalTime.of(12, 0);

    var request2 = new EventRequest(title, day, start,end);

    assertThrows(IllegalArgumentException.class, () -> service.create(request2));
  }
}
