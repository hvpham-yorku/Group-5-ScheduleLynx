package ca.yorku.eecs2311.schedulelynx.web.controller;

import ca.yorku.eecs2311.schedulelynx.domain.Task;
import ca.yorku.eecs2311.schedulelynx.service.TaskService;
import ca.yorku.eecs2311.schedulelynx.web.dto.TaskCreateRequest;
import ca.yorku.eecs2311.schedulelynx.web.dto.TaskResponse;
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
    return taskService.getAll().stream().map(this::toResponse).toList();
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
}
