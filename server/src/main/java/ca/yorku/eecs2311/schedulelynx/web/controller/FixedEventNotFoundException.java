package ca.yorku.eecs2311.schedulelynx.web.controller;

public class FixedEventNotFoundException extends RuntimeException {
  public FixedEventNotFoundException(long id) {
    super("Fixed event not found: " + id);
  }
}
