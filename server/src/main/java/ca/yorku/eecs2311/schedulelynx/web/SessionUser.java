package ca.yorku.eecs2311.schedulelynx.web;

import ca.yorku.eecs2311.schedulelynx.web.controller.AuthController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class SessionUser {
  private SessionUser() {}

  public static long requireUserId(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                        "Login required");
    }
    Object uid = session.getAttribute(AuthController.SESSION_USER_ID);
    if (uid == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                        "Login required");
    }
    return (Long)uid;
  }
}
