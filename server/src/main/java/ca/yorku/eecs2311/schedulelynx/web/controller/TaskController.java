package ca.yorku.eecs2311.schedulelynx.web.controller;

import ca.yorku.eecs2311.schedulelynx.domain.Task;
import ca.yorku.eecs2311.schedulelynx.service.TaskService;
import ca.yorku.eecs2311.schedulelynx.web.controller.errors.TaskNotFoundException;
import ca.yorku.eecs2311.schedulelynx.web.dto.TaskCreateRequest;
import ca.yorku.eecs2311.schedulelynx.web.dto.TaskResponse;
import ca.yorku.eecs2311.schedulelynx.web.dto.TaskUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private final TaskService taskService;

  public TaskController(TaskService taskService) {

    this.taskService = taskService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TaskResponse create(@Valid @RequestBody TaskCreateRequest request) {

    Task created = taskService.create(
        new Task(null, request.getTitle(), request.getDueDate(),
                 request.getEstimatedHours(), request.getDifficulty()));
    return toResponse(created);
  }

  @GetMapping
  public List<TaskResponse> getAll() {

    return taskService.getAll().values().stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  public TaskResponse getById(@PathVariable long id) {

    return taskService.getById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new TaskNotFoundException(id));
  }

  private TaskResponse toResponse(Task task) {

    return new TaskResponse(task.getId(), task.getTitle(), task.getDueDate(),
                            task.getEstimatedHours(), task.getDifficulty());
  }

  @PutMapping("/{id}")
  public TaskResponse update(@PathVariable long id,
                             @Valid @RequestBody TaskUpdateRequest request) {

    Task updated =
        new Task(null, request.getTitle(), request.getDueDate(),
                 request.getEstimatedHours(), request.getDifficulty());

    return taskService.update(id, updated)
        .map(this::toResponse)
        .orElseThrow(() -> new TaskNotFoundException(id));
  }

  @DeleteMapping()
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAll() {

    taskService.deleteAll();
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable long id) {

    boolean deleted = taskService.delete(id);
    if (!deleted) {
      throw new TaskNotFoundException(id);
    }
  }
}
