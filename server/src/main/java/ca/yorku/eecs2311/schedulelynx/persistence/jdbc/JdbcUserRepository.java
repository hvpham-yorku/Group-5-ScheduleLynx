package ca.yorku.eecs2311.schedulelynx.persistence.jdbc;

import ca.yorku.eecs2311.schedulelynx.domain.User;
import ca.yorku.eecs2311.schedulelynx.persistence.UserRepository;
import java.sql.*;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("jdbc")
public class JdbcUserRepository implements UserRepository {

  private final DataSource dataSource;

  public JdbcUserRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public User save(User user) {
    // Insert + return a NEW User with generated id (no setId needed)
    final String sql = "INSERT INTO users (username, password_hash) VALUES "
                       + "(?, ?) RETURNING id";

    try (Connection c = dataSource.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {

      ps.setString(1, user.getUsername());
      ps.setString(2, user.getPasswordHash());

      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) {
          throw new SQLException("Failed to insert user, no id returned");
        }
        long id = rs.getLong("id");
        return new User(id, user.getUsername(), user.getPasswordHash());
      }

    } catch (SQLException e) {
      throw new RuntimeException("Failed to save user", e);
    }
  }

  @Override
  public Optional<User> findByUsername(String username) {
    final String sql =
        "SELECT id, username, password_hash FROM users WHERE username = ?";

    try (Connection c = dataSource.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {

      ps.setString(1, username);

      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next())
          return Optional.empty();

        long id = rs.getLong("id");
        String uname = rs.getString("username");
        String hash = rs.getString("password_hash");
        return Optional.of(new User(id, uname, hash));
      }

    } catch (SQLException e) {
      throw new RuntimeException("Failed to find user by username", e);
    }
  }

  @Override
  public Optional<User> findById(Long id) {
    final String sql =
        "SELECT id, username, password_hash FROM users WHERE id = ?";

    try (Connection c = dataSource.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {

      ps.setLong(1, id);

      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next())
          return Optional.empty();

        long uid = rs.getLong("id");
        String uname = rs.getString("username");
        String hash = rs.getString("password_hash");
        return Optional.of(new User(uid, uname, hash));
      }

    } catch (SQLException e) {
      throw new RuntimeException("Failed to find user by id", e);
    }
  }
}
