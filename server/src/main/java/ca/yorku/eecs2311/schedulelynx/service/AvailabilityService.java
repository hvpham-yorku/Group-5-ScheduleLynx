package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.AvailabilityBlock;
import ca.yorku.eecs2311.schedulelynx.persistence.AvailabilityRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {

  private final AvailabilityRepository repo;

  public AvailabilityService(AvailabilityRepository repo) { this.repo = repo; }

  public AvailabilityBlock create(long userId, AvailabilityBlock block) {
    validate(block);
    return repo.save(userId, block);
  }

  public List<AvailabilityBlock> getAll(long userId) {
    return repo.findAll(userId);
  }

  public Optional<AvailabilityBlock> getById(long userId, long id) {
    return repo.findById(userId, id);
  }

  public Optional<AvailabilityBlock> update(long userId, long id,
                                            AvailabilityBlock updated) {
    validate(updated);
    return repo.update(userId, id, updated);
  }

  public boolean delete(long userId, long id) {
    return repo.delete(userId, id);
  }

  private void validate(AvailabilityBlock block) {
    LocalTime start = block.getStart();
    LocalTime end = block.getEnd();
    if (start == null || end == null || !start.isBefore(end)) {
      throw new IllegalArgumentException("Start time must be before end time");
    }
  }
}
