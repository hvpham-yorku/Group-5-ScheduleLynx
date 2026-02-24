package ca.yorku.eecs2311.schedulelynx.web.controller.errors;

public class TaskNotFoundException extends RuntimeException {

  public TaskNotFoundException(long id) { super("Task not found for ID: " + id); }
}
