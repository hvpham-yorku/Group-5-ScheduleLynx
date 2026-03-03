package ca.yorku.eecs2311.schedulelynx.server.web.controller.errors;

public class EventNotFoundException extends RuntimeException {
  public EventNotFoundException(long id) {
    super("Fixed event not found: " + id);
  }
}
