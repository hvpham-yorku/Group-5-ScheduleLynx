package ca.yorku.eecs2311.schedulelynx.service;

import static org.junit.jupiter.api.Assertions.*;

import ca.yorku.eecs2311.schedulelynx.domain.Difficulty;
import ca.yorku.eecs2311.schedulelynx.domain.Task;
import ca.yorku.eecs2311.schedulelynx.persistence.InMemoryTaskRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TaskServiceTest {

  @Test
  void createTask_assignsId_andStoresTask() {
    TaskService service = new TaskService(new InMemoryTaskRepository());

    Task created = service.create(new Task(
        null, "Test task", LocalDate.of(2026, 2, 13), 3, Difficulty.MEDIUM));

    assertNotNull(created.getId(), "Created task should have an id");
    assertEquals(1L, created.getId());
    assertEquals("Test task", created.getTitle());

    assertEquals(1, service.getAll().size(),
                 "Service should store created task");
  }

  @Test
  void createTask_rejectsEmptyTitle() {
    TaskService service = new TaskService(new InMemoryTaskRepository());

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        ()
            -> service.create(new Task(null, "   ", LocalDate.of(2026, 2, 13),
                                       2, Difficulty.LOW)));

    assertTrue(ex.getMessage().toLowerCase().contains("title"));
  }
}
