package ca.yorku.eecs2311.schedulelynx.web.controller;

public class TaskNotFoundException extends RuntimeException {

  public TaskNotFoundException(long id) { super("Task not found: " + id); }
}
