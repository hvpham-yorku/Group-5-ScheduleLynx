package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.User;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("memory")
@Repository
public class InMemoryUserRepository implements UserRepository {
  private final Map<Long, User> users = new HashMap<>();
  private final AtomicLong nextId = new AtomicLong(1);

  @Override
  public User save(User user) {
    long id = nextId.getAndIncrement();
    User stored = new User(id, user.getUsername(), user.getPasswordHash());
    users.put(id, stored);
    return stored;
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return users.values()
        .stream()
        .filter(u -> u.getUsername().equals(username))
        .findFirst();
  }

  @Override
  public Optional<User> findById(Long id) {
    return Optional.ofNullable(users.get(id));
  }
}
