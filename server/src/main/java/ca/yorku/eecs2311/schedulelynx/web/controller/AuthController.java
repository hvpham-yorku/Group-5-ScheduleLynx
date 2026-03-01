package ca.yorku.eecs2311.schedulelynx.web.controller;

import ca.yorku.eecs2311.schedulelynx.service.UserService;
import ca.yorku.eecs2311.schedulelynx.web.dto.*;
import ca.yorku.eecs2311.schedulelynx.web.dto.LoginRequest;
import ca.yorku.eecs2311.schedulelynx.web.dto.MeResponse;
import ca.yorku.eecs2311.schedulelynx.web.dto.RegisterRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  public static final String SESSION_USER_ID = "uid";
  private final UserService userService;

  public AuthController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public MeResponse register(@Valid @RequestBody RegisterRequest req,
                             HttpSession session) {
    var user = userService.register(req.username(), req.password());
    session.setAttribute(SESSION_USER_ID, user.getId());
    return new MeResponse(user.getId(), user.getUsername());
  }

  @PostMapping("/login")
  public MeResponse login(@Valid @RequestBody LoginRequest req,
                          HttpSession session) {
    var user = userService.login(req.username(), req.password());
    session.setAttribute(SESSION_USER_ID, user.getId());
    return new MeResponse(user.getId(), user.getUsername());
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(HttpSession session) {
    session.invalidate();
  }

  @GetMapping("/me")
  public MeResponse me(HttpSession session) {
    Object uid = session.getAttribute(SESSION_USER_ID);
    if (uid == null)
      throw new IllegalArgumentException("Not logged in");
    var user = userService.findById((Long)uid).orElseThrow(
        () -> new IllegalArgumentException("Not logged in"));
    return new MeResponse(user.getId(), user.getUsername());
  }
}
