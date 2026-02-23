package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.AvailabilityBlock;
import ca.yorku.eecs2311.schedulelynx.persistence.AvailabilityRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {

  private final AvailabilityRepository repository;

  public AvailabilityService(AvailabilityRepository repository) {
    this.repository = repository;
  }

  public AvailabilityBlock create(AvailabilityBlock block) {
    validate(block);
    ensureNoOverlap(block, null);
    return repository.save(block);
  }

  public List<AvailabilityBlock> getAll() { return repository.getAll(); }

  public Optional<AvailabilityBlock> getById(long id) {
    return repository.getById(id);
  }

  public Optional<AvailabilityBlock> update(long id,
                                            AvailabilityBlock updated) {
    validate(updated);
    ensureNoOverlap(updated, id);
    return repository.update(id, updated);
  }

  public boolean delete(long id) { return repository.delete(id); }

  private void validate(AvailabilityBlock block) {
    if (block == null)
      throw new IllegalArgumentException("Availability block must not be null");
    if (block.getDay() == null)
      throw new IllegalArgumentException("Day must not be null");
    if (block.getStart() == null || block.getEnd() == null)
      throw new IllegalArgumentException("Start and end must not be null");

    LocalTime start = block.getStart();
    LocalTime end = block.getEnd();
    if (!start.isBefore(end))
      throw new IllegalArgumentException("Start time must be before end time");
  }

  private void ensureNoOverlap(AvailabilityBlock candidate, Long ignoreId) {
    for (AvailabilityBlock existing : repository.getAll()) {
      if (existing.getId() == null)
        continue;
      if (ignoreId != null && existing.getId().equals(ignoreId))
        continue;
      if (existing.getDay() != candidate.getDay())
        continue;

      boolean overlaps = candidate.getStart().isBefore(existing.getEnd()) &&
                         existing.getStart().isBefore(candidate.getEnd());
      if (overlaps) {
        throw new IllegalArgumentException(
            "Availability block overlaps an existing block");
      }
    }
  }
}
