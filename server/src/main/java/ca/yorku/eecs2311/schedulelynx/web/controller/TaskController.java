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

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody TaskRequest req,
                               HttpServletRequest request) {
        long userId = SessionUser.requireUserId(request);

        Task created = service.create(
                userId, new Task(null, req.title(), req.dueDate(), req.estimatedHours(),
                        req.difficulty()));

        return toResponse(created);
    }

    @GetMapping
    public List<TaskResponse> getAll(HttpServletRequest request) {
        long userId = SessionUser.requireUserId(request);
        return service.getAll(userId).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable long id,
                                HttpServletRequest request) {
        long userId = SessionUser.requireUserId(request);

        return service.getById(userId, id)
                .map(this::toResponse)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable long id,
                               @Valid @RequestBody TaskRequest req,
                               HttpServletRequest request) {
        long userId = SessionUser.requireUserId(request);

        Task updated = new Task(null, req.title(), req.dueDate(),
                req.estimatedHours(), req.difficulty());

        return service.update(userId, id, updated)
                .map(this::toResponse)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id, HttpServletRequest request) {
        long userId = SessionUser.requireUserId(request);

        if (!service.delete(userId, id)) {
            throw new TaskNotFoundException(id);
        }
    }

    private TaskResponse toResponse(Task t) {
        return new TaskResponse(t.getId(), t.getTitle(), t.getDueDate(),
                t.getEstimatedHours(), t.getDifficulty());
    }
}
