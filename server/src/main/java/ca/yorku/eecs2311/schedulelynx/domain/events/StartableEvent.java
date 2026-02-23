package ca.yorku.eecs2311.schedulelynx.domain.events;

import java.time.LocalDateTime;

public interface StartableEvent {

    /** @return the date and time of the start of the event. */
    public LocalDateTime getStartDateTime();

    /** Overwrites the previous date and time of the event's start. */
    public void setStartDateTime(LocalDateTime start);

}
