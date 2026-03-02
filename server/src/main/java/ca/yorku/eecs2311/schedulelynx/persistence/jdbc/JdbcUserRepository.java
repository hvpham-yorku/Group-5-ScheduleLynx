package ca.yorku.eecs2311.schedulelynx.persistence.jdbc;

import ca.yorku.eecs2311.schedulelynx.domain.User;
import ca.yorku.eecs2311.schedulelynx.persistence.UserRepository;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class JdbcUserRepository implements UserRepository {

  private final JdbcTemplate jdbc;

  public JdbcUserRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @Override
  public User save(User user) {
    // If your User has getId() returning Long
    if (user.getId() != null) {
      jdbc.update("UPDATE users SET username=?, password_hash=? WHERE id=?",
                  user.getUsername(), user.getPasswordHash(), user.getId());
      return user;
    }

    KeyHolder kh = new GeneratedKeyHolder();

    jdbc.update(con -> {
      PreparedStatement ps = con.prepareStatement(
          "INSERT INTO users(username, password_hash) VALUES (?, ?)",
          Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, user.getUsername());
      ps.setString(2, user.getPasswordHash());
      return ps;
    }, kh);

    Number key = kh.getKey();
    Long id = (key == null) ? null : key.longValue();

    // IMPORTANT: your User has no setId(), so return a NEW User
    return new User(id, user.getUsername(), user.getPasswordHash());
  }

  @Override
  public Optional<User> findById(Long id) {
    if (id == null)
      return Optional.empty();

    return jdbc
        .query("SELECT id, username, password_hash FROM users WHERE id=?",
               (rs, rowNum)
                   -> new User(rs.getLong("id"), rs.getString("username"),
                               rs.getString("password_hash")),
               id)
        .stream()
        .findFirst();
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return jdbc
        .query("SELECT id, username, password_hash FROM users WHERE username=?",
               (rs, rowNum)
                   -> new User(rs.getLong("id"), rs.getString("username"),
                               rs.getString("password_hash")),
               username)
        .stream()
        .findFirst();
  }
}
