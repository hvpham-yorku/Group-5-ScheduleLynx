package ca.yorku.eecs2311.schedulelynx.service;

import static org.junit.jupiter.api.Assertions.*;

import ca.yorku.eecs2311.schedulelynx.domain.AvailabilityBlock;
import ca.yorku.eecs2311.schedulelynx.domain.Weekday;
import ca.yorku.eecs2311.schedulelynx.persistence.InMemoryAvailabilityRepository;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class AvailabilityServiceTest {

  private AvailabilityService newService() {
    return new AvailabilityService(new InMemoryAvailabilityRepository());
  }

  @Test
  void create_assignsId_andStoresBlock() {
    AvailabilityService service = newService();

    AvailabilityBlock created = service.create(new AvailabilityBlock(
        null, Weekday.MONDAY, LocalTime.of(18, 0), LocalTime.of(21, 0)));

    assertNotNull(created.getId());
    assertEquals(1L, created.getId());
    assertEquals(1, service.getAll().size());
  }

  @Test
  void create_rejectsInvalidTimeRange() {
    AvailabilityService service = newService();

    assertThrows(IllegalArgumentException.class,
                 ()
                     -> service.create(new AvailabilityBlock(
                         null, Weekday.MONDAY, LocalTime.of(21, 0),
                         LocalTime.of(18, 0))));
  }

  @Test
  void create_rejectsOverlapSameDay() {
    AvailabilityService service = newService();

    service.create(new AvailabilityBlock(
        null, Weekday.MONDAY, LocalTime.of(18, 0), LocalTime.of(21, 0)));

    assertThrows(IllegalArgumentException.class,
                 ()
                     -> service.create(new AvailabilityBlock(
                         null, Weekday.MONDAY, LocalTime.of(20, 0),
                         LocalTime.of(22, 0))));
  }
}
