package ca.yorku.eecs2311.schedulelynx.server.persistence;

import ca.yorku.eecs2311.schedulelynx.server.domain.AvailabilityBlock;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryAvailabilityRepository implements AvailabilityRepository {

  private final Map<Long, List<AvailabilityBlock>> byUser =
      new ConcurrentHashMap<>();
  private final AtomicLong nextId = new AtomicLong(1);

  @Override
  public AvailabilityBlock save(long userId, AvailabilityBlock block) {
    AvailabilityBlock stored =
        new AvailabilityBlock(nextId.getAndIncrement(), block.getDay(),
                              block.getStart(), block.getEnd());

    byUser.computeIfAbsent(userId, k -> new ArrayList<>()).add(stored);
    return stored;
  }

  @Override
  public List<AvailabilityBlock> findAll(long userId) {
    return new ArrayList<>(byUser.getOrDefault(userId, List.of()));
  }

  @Override
  public Optional<AvailabilityBlock> findById(long userId, long id) {
    return byUser.getOrDefault(userId, List.of())
        .stream()
        .filter(b -> b.getId() != null && b.getId() == id)
        .findFirst();
  }

  @Override
  public Optional<AvailabilityBlock> update(long userId, long id,
                                            AvailabilityBlock updated) {
    List<AvailabilityBlock> blocks = byUser.get(userId);
    if (blocks == null)
      return Optional.empty();

    for (int i = 0; i < blocks.size(); i++) {
      AvailabilityBlock existing = blocks.get(i);
      if (existing.getId() != null && existing.getId() == id) {
        AvailabilityBlock stored = new AvailabilityBlock(
            id, updated.getDay(), updated.getStart(), updated.getEnd());
        blocks.set(i, stored);
        return Optional.of(stored);
      }
    }
    return Optional.empty();
  }

  @Override
  public boolean delete(long userId, long id) {
    List<AvailabilityBlock> blocks = byUser.get(userId);
    if (blocks == null)
      return false;
    return blocks.removeIf(b -> b.getId() != null && b.getId() == id);
  }
}
