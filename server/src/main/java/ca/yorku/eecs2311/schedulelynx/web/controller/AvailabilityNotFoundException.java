package ca.yorku.eecs2311.schedulelynx.web.controller;

public class AvailabilityNotFoundException extends RuntimeException {
  public AvailabilityNotFoundException(long id) {
    super("Availability block not found: " + id);
  }
}
