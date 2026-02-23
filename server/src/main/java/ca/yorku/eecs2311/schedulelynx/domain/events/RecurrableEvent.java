package ca.yorku.eecs2311.schedulelynx.domain.events;

import jakarta.annotation.Nullable;

public interface RecurrableEvent {

    /** @return the {@link Recurrence} of the event. */
    Recurrence getRecurrence();

    /** Overwrites the previous {@link Recurrence} of the event.<br>
     *  If {@code null} provided, recurrence will be set to NONE. */
    void setRecurrence(@Nullable Recurrence recurrence);

}
