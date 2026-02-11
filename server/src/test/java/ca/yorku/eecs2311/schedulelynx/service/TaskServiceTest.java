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

  private TaskService newService() {
    return new TaskService(new InMemoryTaskRepository());
  }

  @Test
  void update_existingTask_updatesFields() {
    TaskService service = newService();

    Task created = service.create(
        new Task(null, "Old", LocalDate.of(2026, 2, 13), 5, Difficulty.HIGH));

    Task update =
        new Task(null, "New", LocalDate.of(2026, 2, 14), 3, Difficulty.MEDIUM);

    Task updated = service.update(created.getId(), update).orElseThrow();

    assertEquals(created.getId(), updated.getId());
    assertEquals("New", updated.getTitle());
    assertEquals(LocalDate.of(2026, 2, 14), updated.getDueDate());
    assertEquals(3, updated.getEstimatedHours());
    assertEquals(Difficulty.MEDIUM, updated.getDifficulty());
  }

  @Test
  void update_missingTask_returnsEmpty() {
    TaskService service = newService();

    Task update =
        new Task(null, "New", LocalDate.of(2026, 2, 14), 3, Difficulty.MEDIUM);

    assertTrue(service.update(999L, update).isEmpty());
  }

  @Test
  void delete_existingTask_removesIt() {
    TaskService service = newService();

    Task created = service.create(new Task(
        null, "To delete", LocalDate.of(2026, 2, 20), 2, Difficulty.LOW));

    assertTrue(service.delete(created.getId()));
    assertTrue(service.getById(created.getId()).isEmpty());
    assertEquals(0, service.getAll().size());
  }

  @Test
  void delete_missingTask_returnsFalse() {
    TaskService service = newService();
    assertFalse(service.delete(999L));
  }
}
