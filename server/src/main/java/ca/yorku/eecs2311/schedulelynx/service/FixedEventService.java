package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.FixedEvent;
import ca.yorku.eecs2311.schedulelynx.persistence.FixedEventRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class FixedEventService {

    private final FixedEventRepository repository;

    public FixedEventService(FixedEventRepository repository) {
        this.repository = repository;
    }

    public FixedEvent create(FixedEvent event) {
        validate(event);
        ensureNoOverlap(event, null);
        return repository.save(event);
    }

    public List<FixedEvent> getAll() { return repository.findAll(); }

    public Optional<FixedEvent> getById(long id) {
        return repository.findById(id);
    }

    public Optional<FixedEvent> update(long id, FixedEvent updated) {
        validate(updated);
        ensureNoOverlap(updated, id);
        return repository.update(id, updated);
    }

    public boolean delete(long id) { return repository.delete(id); }

    private void validate(FixedEvent event) {
        if (event == null)
            throw new IllegalArgumentException("Fixed event must not be null");
        if (event.getTitle() == null || event.getTitle().trim().isEmpty())
            throw new IllegalArgumentException("Title must not be empty");
        if (event.getDay() == null)
            throw new IllegalArgumentException("Day must not be null");
        if (event.getStart() == null || event.getEnd() == null)
            throw new IllegalArgumentException("Start and end must not be null");

        LocalTime start = event.getStart();
        LocalTime end = event.getEnd();
        if (!start.isBefore(end))
            throw new IllegalArgumentException("Start time must be before end time");
    }

    private void ensureNoOverlap(FixedEvent candidate, Long ignoreId) {
        for (FixedEvent existing : repository.findAll()) {
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
                        "Fixed event overlaps an existing event");
            }
        }
    }
}
