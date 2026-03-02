package ca.yorku.eecs2311.schedulelynx.service;

import static org.junit.jupiter.api.Assertions.*;

import ca.yorku.eecs2311.schedulelynx.domain.Difficulty;
import ca.yorku.eecs2311.schedulelynx.domain.Task;
import ca.yorku.eecs2311.schedulelynx.persistence.InMemoryTaskRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskServiceTest {

  private static final long USER_ID = 1L;

  private TaskService service;

  @BeforeEach
  void setup() {
    service = new TaskService(new InMemoryTaskRepository());
  }

  @Test
  void create_and_getAll_work() {
    service.create(USER_ID, new Task(null, "ITR1", LocalDate.of(2026, 2, 13), 6,
                                     Difficulty.HIGH));

    var all = service.getAll(USER_ID);
    assertEquals(1, all.size());
    assertEquals("ITR1", all.get(0).getTitle());
  }

  @Test
  void create_missingTitle_throws() {
    var bad =
        new Task(null, "   ", LocalDate.of(2026, 2, 13), 2, Difficulty.LOW);
    assertThrows(IllegalArgumentException.class,
                 () -> service.create(USER_ID, bad));
  }

  @Test
  void update_work() {
    var created =
        service.create(USER_ID, new Task(null, "Old", LocalDate.of(2026, 2, 13),
                                         2, Difficulty.MEDIUM));

    var updated =
        new Task(null, "New", LocalDate.of(2026, 2, 14), 3, Difficulty.HIGH);

    var updatedOpt = service.update(USER_ID, created.getId(), updated);
    assertTrue(updatedOpt.isPresent());
    assertEquals("New", updatedOpt.get().getTitle());
  }

  @Test
  void delete_work() {
    var created = service.create(USER_ID, new Task(null, "Delete me",
                                                   LocalDate.of(2026, 2, 13), 1,
                                                   Difficulty.LOW));

    assertTrue(service.delete(USER_ID, created.getId()));
    assertTrue(service.getAll(USER_ID).isEmpty());
  }
}
