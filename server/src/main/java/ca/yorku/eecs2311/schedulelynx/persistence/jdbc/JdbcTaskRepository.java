package ca.yorku.eecs2311.schedulelynx.persistence.jdbc;

import ca.yorku.eecs2311.schedulelynx.domain.Difficulty;
import ca.yorku.eecs2311.schedulelynx.domain.Task;
import ca.yorku.eecs2311.schedulelynx.persistence.TaskRepository;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class JdbcTaskRepository implements TaskRepository {

  private final JdbcTemplate jdbc;

  public JdbcTaskRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @Override
  public Task save(long userId, Task task) {
    KeyHolder kh = new GeneratedKeyHolder();

    jdbc.update(con -> {
      PreparedStatement ps = con.prepareStatement(
          "INSERT INTO tasks(user_id, title, due_date, estimated_hours, "
              + "difficulty) VALUES (?,?,?,?,?)",
          Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, userId);
      ps.setString(2, task.getTitle());
      ps.setObject(3, task.getDueDate());
      ps.setInt(4, task.getEstimatedHours());
      ps.setString(5, task.getDifficulty().name());
      return ps;
    }, kh);

    Number key = kh.getKey();
    Long id = (key == null) ? null : key.longValue();
    return new Task(id, task.getTitle(), task.getDueDate(),
                    task.getEstimatedHours(), task.getDifficulty());
  }

  @Override
  public List<Task> findAll(long userId) {
    return jdbc.query(
        "SELECT id, title, due_date, estimated_hours, difficulty FROM tasks "
            + "WHERE user_id=? ORDER BY id",
        (rs, rowNum)
            -> new Task(rs.getLong("id"), rs.getString("title"),
                        rs.getObject("due_date", LocalDate.class),
                        rs.getInt("estimated_hours"),
                        Difficulty.valueOf(rs.getString("difficulty"))),
        userId);
  }

  @Override
  public Optional<Task> findById(long userId, long id) {
    return jdbc
        .query("SELECT id, title, due_date, estimated_hours, difficulty FROM "
                   + "tasks WHERE user_id=? AND id=?",
               (rs, rowNum)
                   -> new Task(rs.getLong("id"), rs.getString("title"),
                               rs.getObject("due_date", LocalDate.class),
                               rs.getInt("estimated_hours"),
                               Difficulty.valueOf(rs.getString("difficulty"))),
               userId, id)
        .stream()
        .findFirst();
  }

  @Override
  public Optional<Task> update(long userId, long id, Task updated) {
    int rows = jdbc.update(
        "UPDATE tasks SET title=?, due_date=?, estimated_hours=?, "
            + "difficulty=? WHERE user_id=? AND id=?",
        updated.getTitle(), updated.getDueDate(), updated.getEstimatedHours(),
        updated.getDifficulty().name(), userId, id);

    if (rows == 0)
      return Optional.empty();
    return findById(userId, id);
  }

  @Override
  public boolean delete(long userId, long id) {
    return jdbc.update("DELETE FROM tasks WHERE user_id=? AND id=?", userId,
                       id) > 0;
  }
}
