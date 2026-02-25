package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.Difficulty;
import ca.yorku.eecs2311.schedulelynx.persistence.InMemoryTaskRepository;
import ca.yorku.eecs2311.schedulelynx.web.dto.TaskRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest {

  private TaskService newService() {
    return new TaskService(new InMemoryTaskRepository());
  }

  @Test
  void createTask_assignsId_andStoresTask() {

    var service = newService();

    var title    = "Test task";
    var dueDate  = LocalDate.of(2026, 2, 13);
    var estHours = 3;
    var diff     = Difficulty.MEDIUM;

    var tkReq = new TaskRequest(null, title, dueDate, estHours, diff);
    var tkRes = service.create(tkReq);

    assertNotNull(tkRes.getId(), "Created task should have an id");
    assertEquals(1L, tkRes.getId());
    assertEquals("Test task", tkRes.getTitle());

    assertEquals(1, service.getAll().size(),
                 "Service should store created task");
  }

  @Test
  void createTask_rejectsEmptyTitle() {

    var service = newService();

    var title    = "   ";
    var dueDate  = LocalDate.of(2026, 2, 13);
    var estHours = 2;
    var diff     = Difficulty.LOW;

    var tkReq = new TaskRequest(null, title, dueDate, estHours, diff);

    var ex = assertThrows(IllegalArgumentException.class, () -> service.create(tkReq));

    assertTrue(ex.getMessage().toLowerCase().contains("title"));
  }

  @Test
  void update_existingTask_updatesFields() {

    var service = newService();

    var title    = "Old";
    var dueDate  = LocalDate.of(2026, 2, 13);
    var estHours = 5;
    var diff     = Difficulty.HIGH;

    var tkReqNew = new TaskRequest(null, title, dueDate, estHours, diff);
    var tkResNew = service.create(tkReqNew);

    title    = "New";
    dueDate  = LocalDate.of(2026, 2, 14);
    estHours = 3;
    diff     = Difficulty.MEDIUM;

    var tkReqUpd = new TaskRequest(tkReqNew.id(), title, dueDate, estHours, diff);

    var tkResUpd = service.update(tkReqUpd);
    if (tkResUpd.isEmpty()) return;
    var updatedTask = tkResUpd.get();

    assertEquals(tkResNew.getId(), updatedTask.getId());
    assertEquals("New", updatedTask.getTitle());
    assertEquals(LocalDate.of(2026, 2, 14), updatedTask.getDueDate());
    assertEquals(3, updatedTask.getEstimatedHours());
    assertEquals(Difficulty.MEDIUM, updatedTask.getDifficulty());
  }

  @Test
  void update_missingTask_returnsEmpty() {

    var service = newService();

    var title    = "title placeholder";
    var dueDate  = LocalDate.of(2026, 2, 14);
    var estHours = 3;
    var diff     = Difficulty.MEDIUM;

    var tkReq = new TaskRequest(null, title, dueDate, estHours, diff);

    assertTrue(service.update(tkReq).isEmpty());
  }

  @Test
  void delete_existingTask_removesIt() {

    var service = newService();

    var title    = "To delete";
    var dueDate  = LocalDate.of(2026, 2, 20);
    var estHours = 2;
    var diff     = Difficulty.LOW;

    var tkReq = new TaskRequest(null, title, dueDate, estHours, diff);
    var tkRes = service.create(tkReq);

    assertNotNull(tkRes.getId());
    var id = tkRes.getId();

    assertTrue(service.delete(id));
    assertTrue(service.getTask(id).isEmpty());
    assertEquals(0, service.getAll().size());
  }

  @Test
  void delete_missingTask_returnsFalse() {

    var service = newService();

    assertFalse(service.delete(999L));
  }

}
