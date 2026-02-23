package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.OneTimeEvent;
import ca.yorku.eecs2311.schedulelynx.persistence.OneTimeEventRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import ca.yorku.eecs2311.schedulelynx.web.dto.OneTimeEventRequest;
import org.springframework.stereotype.Service;

@Service
public class OneTimeEventService {

    private final OneTimeEventRepository repository;

    public OneTimeEventService(OneTimeEventRepository repository) {
        this.repository = repository;
    }

    public OneTimeEvent create(OneTimeEventRequest req) {

        var event = new OneTimeEvent(null, req.title(), req.day(),req.start(), req.end());

        validate(event);
        ensureNoOverlap(event, null);
        return repository.save(event);
    }

    public List<OneTimeEvent> getAll() { return repository.findAll(); }

    public Optional<OneTimeEvent> getById(long id) {
        return repository.findById(id);
    }

    public Optional<OneTimeEvent> update(long id, OneTimeEvent updated) {
        validate(updated);
        ensureNoOverlap(updated, id);
        return repository.update(id, updated);
    }

    public boolean delete(long id) { return repository.delete(id); }

    private void validate(OneTimeEvent event) {
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

    private void ensureNoOverlap(OneTimeEvent candidate, Long ignoreId) {
        for (OneTimeEvent existing : repository.findAll()) {
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
