package ca.yorku.eecs2311.schedulelynx.persistence.jdbc;

import ca.yorku.eecs2311.schedulelynx.domain.Event;
import ca.yorku.eecs2311.schedulelynx.domain.Weekday;
import ca.yorku.eecs2311.schedulelynx.persistence.EventRepository;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Profile("jdbc")
@Repository
public class JdbcEventRepository implements EventRepository {

  private final JdbcTemplate jdbc;

  public JdbcEventRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @Override
  public Event save(long userId, Event event) {
    KeyHolder kh = new GeneratedKeyHolder();

    jdbc.update(con -> {
      PreparedStatement ps =
          con.prepareStatement("INSERT INTO events(user_id, title, day, "
                                   + "start_time, end_time) VALUES (?,?,?,?,?)",
                               Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, userId);
      ps.setString(2, event.getTitle());
      ps.setString(3, event.getDay().name());
      ps.setObject(4, event.getStart());
      ps.setObject(5, event.getEnd());
      return ps;
    }, kh);

    Number key = kh.getKey();
    Long id = (key == null) ? null : key.longValue();
    return new Event(id, event.getTitle(), event.getDay(), event.getStart(),
                     event.getEnd());
  }

  @Override
  public List<Event> findAll(long userId) {
    return jdbc.query(
        "SELECT id, title, day, start_time, end_time FROM events WHERE "
            + "user_id=? ORDER BY id",
        (rs, rowNum)
            -> new Event(rs.getLong("id"), rs.getString("title"),
                         Weekday.valueOf(rs.getString("day")),
                         rs.getObject("start_time", LocalTime.class),
                         rs.getObject("end_time", LocalTime.class)),
        userId);
  }

  @Override
  public Optional<Event> findById(long userId, long id) {
    return jdbc
        .query("SELECT id, title, day, start_time, end_time FROM events "
                   + "WHERE user_id=? AND id=?",
               (rs, rowNum)
                   -> new Event(rs.getLong("id"), rs.getString("title"),
                                Weekday.valueOf(rs.getString("day")),
                                rs.getObject("start_time", LocalTime.class),
                                rs.getObject("end_time", LocalTime.class)),
               userId, id)
        .stream()
        .findFirst();
  }

  @Override
  public Optional<Event> update(long userId, long id, Event updated) {
    int rows = jdbc.update("UPDATE events SET title=?, day=?, start_time=?, "
                               + "end_time=? WHERE user_id=? AND id=?",
                           updated.getTitle(), updated.getDay().name(),
                           updated.getStart(), updated.getEnd(), userId, id);

    if (rows == 0)
      return Optional.empty();
    return findById(userId, id);
  }

  @Override
  public boolean delete(long userId, long id) {
    return jdbc.update("DELETE FROM events WHERE user_id=? AND id=?", userId,
                       id) > 0;
  }
}
