package ca.yorku.eecs2311.schedulelynx.persistence.jdbc;

import ca.yorku.eecs2311.schedulelynx.domain.AvailabilityBlock;
import ca.yorku.eecs2311.schedulelynx.domain.Weekday;
import ca.yorku.eecs2311.schedulelynx.persistence.AvailabilityRepository;
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
public class JdbcAvailabilityRepository implements AvailabilityRepository {

  private final JdbcTemplate jdbc;

  public JdbcAvailabilityRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @Override
  public AvailabilityBlock save(long userId, AvailabilityBlock block) {
    KeyHolder kh = new GeneratedKeyHolder();

    jdbc.update(con -> {
      PreparedStatement ps = con.prepareStatement(
          "INSERT INTO availability_blocks(user_id, "
              + "day, start_time, end_time) VALUES (?,?,?,?)",
          Statement.RETURN_GENERATED_KEYS);
      ps.setLong(1, userId);
      ps.setString(2, block.getDay().name());
      ps.setObject(3, block.getStart());
      ps.setObject(4, block.getEnd());
      return ps;
    }, kh);

    Number key = kh.getKey();
    Long id = (key == null) ? null : key.longValue();
    return new AvailabilityBlock(id, block.getDay(), block.getStart(),
                                 block.getEnd());
  }

  @Override
  public List<AvailabilityBlock> findAll(long userId) {
    return jdbc.query("SELECT id, day, start_time, end_time FROM "
                          + "availability_blocks WHERE user_id=? ORDER BY id",
                      (rs, rowNum)
                          -> new AvailabilityBlock(
                              rs.getLong("id"),
                              Weekday.valueOf(rs.getString("day")),
                              rs.getObject("start_time", LocalTime.class),
                              rs.getObject("end_time", LocalTime.class)),
                      userId);
  }

  @Override
  public Optional<AvailabilityBlock> findById(long userId, long id) {
    return jdbc
        .query("SELECT id, day, start_time, end_time FROM "
                   + "availability_blocks WHERE user_id=? AND id=?",
               (rs, rowNum)
                   -> new AvailabilityBlock(
                       rs.getLong("id"), Weekday.valueOf(rs.getString("day")),
                       rs.getObject("start_time", LocalTime.class),
                       rs.getObject("end_time", LocalTime.class)),
               userId, id)
        .stream()
        .findFirst();
  }

  @Override
  public Optional<AvailabilityBlock> update(long userId, long id,
                                            AvailabilityBlock updated) {
    int rows =
        jdbc.update("UPDATE availability_blocks SET day=?, "
                        + "start_time=?, end_time=? WHERE user_id=? AND id=?",
                    updated.getDay().name(), updated.getStart(),
                    updated.getEnd(), userId, id);

    if (rows == 0)
      return Optional.empty();
    return findById(userId, id);
  }

  @Override
  public boolean delete(long userId, long id) {
    return jdbc.update(
               "DELETE FROM availability_blocks WHERE user_id=? AND id=?",
               userId, id) > 0;
  }
}
