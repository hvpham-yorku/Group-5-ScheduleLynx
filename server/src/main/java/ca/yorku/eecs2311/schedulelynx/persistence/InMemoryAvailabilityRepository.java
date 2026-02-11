package ca.yorku.eecs2311.schedulelynx.persistence;

import ca.yorku.eecs2311.schedulelynx.domain.AvailabilityBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryAvailabilityRepository implements AvailabilityRepository {

  private final List<AvailabilityBlock> blocks = new ArrayList<>();
  private final AtomicLong nextId = new AtomicLong(1);

  @Override
  public AvailabilityBlock save(AvailabilityBlock block) {
    AvailabilityBlock stored =
        new AvailabilityBlock(nextId.getAndIncrement(), block.getDay(),
                              block.getStart(), block.getEnd());
    blocks.add(stored);
    return stored;
  }

  @Override
  public List<AvailabilityBlock> findAll() {
    return new ArrayList<>(blocks);
  }

  @Override
  public Optional<AvailabilityBlock> findById(long id) {
    return blocks.stream()
        .filter(b -> b.getId() != null && b.getId() == id)
        .findFirst();
  }

  @Override
  public Optional<AvailabilityBlock> update(long id,
                                            AvailabilityBlock updated) {
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
  public boolean delete(long id) {
    return blocks.removeIf(b -> b.getId() != null && b.getId() == id);
  }
}
