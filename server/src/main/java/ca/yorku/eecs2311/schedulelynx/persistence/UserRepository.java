package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.User;
import java.util.Optional;

public interface UserRepository {
  User save(User user);

  Optional<User> findByUsername(String username);

  Optional<User> findById(Long id);
}
