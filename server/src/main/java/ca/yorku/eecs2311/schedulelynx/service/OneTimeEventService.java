package ca.yorku.eecs2311.schedulelynx.service;

import ca.yorku.eecs2311.schedulelynx.domain.OneTimeEvent;
import ca.yorku.eecs2311.schedulelynx.persistence.OneTimeEventRepository;
import ca.yorku.eecs2311.schedulelynx.web.dto.OneTimeEventRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OneTimeEventService {

    private final OneTimeEventRepository repo;

    public OneTimeEventService(OneTimeEventRepository repo) {

        this.repo = repo;
    }

    public OneTimeEvent create(OneTimeEventRequest req) {

        var event = new OneTimeEvent(null, req.title(), req.day(),req.start(), req.end());

        validate(event);
        return repo.save(event);
    }

    public List<OneTimeEvent> getAll() {

        return repo.getAllEvents();
    }

    public Optional<OneTimeEvent> getById(long id) {

        return repo.getEventByID(id);
    }

    public Optional<OneTimeEvent> update(long id, OneTimeEvent updated) {

        validate(updated);
        return repo.update(id, updated);
    }

    public boolean delete(long id) {

        return repo.delete(id);
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

        if (start.isBefore(end)) checkForOverlap(event);
        else throw new IllegalArgumentException("Start time must be before end time");
    }

    private void checkForOverlap(OneTimeEvent candidate) {

        var ignorableID = candidate.getId();

        for (var existingEvent : repo.getAllEvents()) {

            var id  = existingEvent.getId();
            var day = existingEvent.getDay();

            if (id == null)                continue;
            if (id.equals(ignorableID))    continue;
            if (day != candidate.getDay()) continue;

            var candStart  = candidate.getStart();
            var candEnd    = candidate.getEnd();
            var existStart = existingEvent.getStart();
            var existEnd   = existingEvent.getEnd();

            boolean isOverlapping = candStart.isBefore(existEnd) && existStart.isBefore(candEnd);

            if (isOverlapping)
                throw new IllegalArgumentException(
                        "Fixed event overlaps an existing event");
        }
    }
}
