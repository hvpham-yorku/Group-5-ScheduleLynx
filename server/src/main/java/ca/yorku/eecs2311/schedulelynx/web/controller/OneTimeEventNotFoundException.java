package ca.yorku.eecs2311.schedulelynx.web.controller;

public class OneTimeEventNotFoundException extends RuntimeException {
  public OneTimeEventNotFoundException(long id) {
    super("Fixed event not found: " + id);
  }
}
