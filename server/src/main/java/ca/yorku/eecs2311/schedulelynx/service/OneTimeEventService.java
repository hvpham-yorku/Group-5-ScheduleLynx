package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.OneTimeEvent;
import ca.yorku.eecs2311.schedulelynx.persistence.OneTimeEventRepository;

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

    public List<OneTimeEvent> getAll() {

        return repository.getAllEvents();
    }

    public Optional<OneTimeEvent> getById(long id) {

        return repository.getEventByID(id);
    }

    public Optional<OneTimeEvent> update(long id, OneTimeEvent updated) {

        validate(updated);
        ensureNoOverlap(updated, id);
        return repository.update(id, updated);
    }

    public boolean delete(long id) {

        return repository.delete(id);
    }

    private void validate(OneTimeEvent event) {

        if (event == null) throw new IllegalArgumentException("Fixed event must not be null");

        var title = event.getTitle();
        var day   = event.getDay();
        var start = event.getStart();
        var end   = event.getEnd();

        if (title == null  ) throw new IllegalArgumentException("Title must not be null");
        if (title.isBlank()) throw new IllegalArgumentException("Title must not be blank");
        if (day   == null  ) throw new IllegalArgumentException("Day must not be null");
        if (start == null  ) throw new IllegalArgumentException("Start must not be null");
        if (end   == null  ) throw new IllegalArgumentException("End must not be null");

        if (start.isBefore(end)) return;
        throw new IllegalArgumentException("Start time must be before end time");
    }

    private void ensureNoOverlap(OneTimeEvent candidate, Long ignoreId) {

        for (OneTimeEvent existing : repository.getAllEvents()) {
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
