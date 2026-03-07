package ca.yorku.eecs2311.schedulelynx.web.controller;

import ca.yorku.eecs2311.schedulelynx.domain.Task;
import ca.yorku.eecs2311.schedulelynx.service.TaskService;
import ca.yorku.eecs2311.schedulelynx.web.SessionUser;
import ca.yorku.eecs2311.schedulelynx.web.controller.errors.TaskNotFoundException;
import ca.yorku.eecs2311.schedulelynx.web.dto.TaskRequest;
import ca.yorku.eecs2311.schedulelynx.web.dto.TaskResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody TaskRequest req,
                         HttpServletRequest request) {

        long userID = SessionUser.requireUserId(request);

        var task = service.create(userID, req);
        return toResponse(task);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TaskResponse update(@Valid @RequestBody TaskRequest req,
                           HttpServletRequest request) {

        long userId = SessionUser.requireUserId(request);

        if (req == null)      throw new IllegalArgumentException("Task request cannot be null!");
        if (req.id() == null) throw new IllegalArgumentException("Task request ID must not be null!");

        var task = service.update(req);

        if (task.isEmpty()) throw new TaskNotFoundException(req.id());

        return toResponse(task.get(request));
    }

    @GetMapping
    public List<TaskResponse> getAll(HttpServletRequest request) {

        return service.getAll().values().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public TaskResponse getTask(@PathVariable long id, HttpServletRequest request) {

        return service.getTask(id)
            .map(this::toResponse)
            .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @DeleteMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll(HttpServletRequest request) {

        service.deleteAll();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id,
                       HttpServletRequest request) {

   long userId = SessionUser.requireUserId(request);

    boolean deleted = service.delete(id);
    if (!deleted) throw new TaskNotFoundException(id);

    }

    private TaskResponse toResponse(Task task) {

        return new TaskResponse(task.getId(), task.getTitle(), task.getDueDate(),
                                task.getEstimatedHours(), task.getDifficulty());
    }
}
