package ca.yorku.eecs2311.schedulelynx.web.controller;

import ca.yorku.eecs2311.schedulelynx.domain.Task;
import ca.yorku.eecs2311.schedulelynx.service.TaskService;
import ca.yorku.eecs2311.schedulelynx.web.SessionUser;
import ca.yorku.eecs2311.schedulelynx.web.dto.TaskRequest;
import ca.yorku.eecs2311.schedulelynx.web.dto.TaskResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private final TaskService taskService;

  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  @GetMapping
  public List<TaskResponse> getAll(HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);
    return taskService.getAll(userId).stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  public TaskResponse getById(@PathVariable long id,
                              HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);
    return toResponse(taskService.getById(userId, id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TaskResponse create(@Valid @RequestBody TaskRequest body,
                             HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);

    Task task = new Task();
    task.setTitle(body.title());
    task.setDueDate(body.dueDate());
    task.setEstimatedHours(body.estimatedHours());
    task.setDifficulty(body.difficulty());
    task.setPreferredStartTime(body.preferredStartTime());
    task.setPreferredEndTime(body.preferredEndTime());
    task.setMaxHoursPerDay(body.maxHoursPerDay());
    task.setMinBlockHours(body.minBlockHours());
    task.setMaxBlockHours(body.maxBlockHours());

    return toResponse(taskService.create(userId, task));
  }

  @PutMapping("/{id}")
  public TaskResponse update(@PathVariable long id,
                             @Valid @RequestBody TaskRequest body,
                             HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);

    Task task = new Task();
    task.setTitle(body.title());
    task.setDueDate(body.dueDate());
    task.setEstimatedHours(body.estimatedHours());
    task.setDifficulty(body.difficulty());
    task.setPreferredStartTime(body.preferredStartTime());
    task.setPreferredEndTime(body.preferredEndTime());
    task.setMaxHoursPerDay(body.maxHoursPerDay());
    task.setMinBlockHours(body.minBlockHours());
    task.setMaxBlockHours(body.maxBlockHours());

    return toResponse(taskService.update(userId, id, task));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long id, HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);
    taskService.delete(userId, id);
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAll(HttpServletRequest request) {
    long userId = SessionUser.requireUserId(request);
    taskService.deleteAll(userId);
  }

  private TaskResponse toResponse(Task task) {
    return new TaskResponse(task.getId(), task.getTitle(), task.getDueDate(),
                            task.getEstimatedHours(), task.getDifficulty(),
                            task.getPreferredStartTime(),
                            task.getPreferredEndTime(),
                            task.getMaxHoursPerDay(), task.getMinBlockHours(),
                            task.getMaxBlockHours());
  }
}
