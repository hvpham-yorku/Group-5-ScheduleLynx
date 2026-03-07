package ca.yorku.eecs2311.schedulelynx.web;

import ca.yorku.eecs2311.schedulelynx.web.controller.AuthController;
import jakarta.servlet.http.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest req, HttpServletResponse res,
                           Object handler) throws Exception {
    String path = req.getRequestURI();
    if (path.startsWith("/api/auth"))
      return true;

    HttpSession session = req.getSession(false);
    if (session == null ||
        session.getAttribute(AuthController.SESSION_USER_ID) == null) {
      res.setStatus(HttpStatus.UNAUTHORIZED.value());
      res.setContentType("application/json");
      res.getWriter().write(
          "{\"error\":\"UNAUTHORIZED\",\"message\":\"Login required\"}");
      return false;
    }
    return true;
  }
}
