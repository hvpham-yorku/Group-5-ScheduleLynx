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

  public User register(String username, String password) {
    if (repo.findByUsername(username).isPresent()) {
      throw new IllegalArgumentException("Username already exists");
    }
    String hash = encoder.encode(password);
    return repo.save(new User(null, username, hash));
  }

  public User login(String username, String password) {
    User user = repo.findByUsername(username).orElseThrow(
        () -> new IllegalArgumentException("Invalid username or password"));

    if (!encoder.matches(password, user.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid username or password");
    }
    return user;
  }

  public Optional<User> findById(Long id) { return repo.findById(id); }
}
