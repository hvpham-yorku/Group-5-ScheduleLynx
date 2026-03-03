package ca.yorku.eecs2311.schedulelynx.server.web.controller.errors;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, String>>
  handleStatus(ResponseStatusException ex) {
    HttpStatus status = (HttpStatus)ex.getStatusCode();
    String msg =
        ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();

    return ResponseEntity.status(status).body(
        Map.of("error", status.name(), "message", msg));
  }
}
