package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.User;
import ca.yorku.eecs2311.schedulelynx.persistence.UserRepository;
import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository repo;
  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  public UserService(UserRepository repo) { this.repo = repo; }

  public User register(String username, String email, String fullName,
                       String password) {
    String normalizedUsername = username.trim();
    String normalizedEmail = email.trim().toLowerCase();
    String normalizedFullName = fullName.trim();

    if (repo.existsByUsername(normalizedUsername)) {
      throw new IllegalArgumentException("Username already exists");
    }

    if (repo.existsByEmail(normalizedEmail)) {
      throw new IllegalArgumentException("Email already exists");
    }

    String hash = encoder.encode(password);
    User user =
        new User(normalizedUsername, normalizedEmail, normalizedFullName, hash);
    return repo.save(user);
  }

  public User login(String usernameOrEmail, String password) {
    String login = usernameOrEmail.trim();

    User user = repo.findByUsername(login)
                    .or(() -> repo.findByEmail(login.toLowerCase()))
                    .orElseThrow(()
                                     -> new IllegalArgumentException(
                                         "Invalid username/email or password"));

    if (!encoder.matches(password, user.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid username/email or password");
    }

    return user;
  }

  public Optional<User> findById(Long id) { return repo.findById(id); }
}
