package ca.yorku.eecs2311.schedulelynx.web.controller;

public class EventNotFoundException extends RuntimeException {
  public EventNotFoundException(long id) {
    super("Fixed event not found: " + id);
  }
}
