package ca.yorku.eecs2311.schedulelynx.server.web.controller.errors;

public class AvailabilityNotFoundException extends RuntimeException {
  public AvailabilityNotFoundException(long id) {
    super("Availability block not found: " + id);
  }
}
