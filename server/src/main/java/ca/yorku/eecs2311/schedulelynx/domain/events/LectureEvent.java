package ca.yorku.eecs2311.schedulelynx.domain.events;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Contains the data for Lecture-type events.
 */
public class LectureEvent extends AbstractEvent
        implements StartableEvent, RecurrableEvent {

    protected LocalDateTime start;
    protected Recurrence recurrence;

    public LectureEvent() {}

    public LectureEvent(
            @NotBlank long id,
            @NotNull  EventType type,
            @NotNull  String name,
            @Nullable String desc,
            @NotNull  LocalDateTime start,
            @NotNull  LocalDateTime end,
            @Nullable Recurrence recurrence,
            @Nullable Difficulty difficulty)
    {
        super(id, type, name, desc, end, difficulty);

        this.start = start;
        this.recurrence = (recurrence != null) ? recurrence : Recurrence.NONE;

        if (type == EventType.LECTURE) return;
        throw new IllegalArgumentException("""
                Type != 'LECTURE'.
                Make sure you're calling the right class for:\s""" + type.name()
        );
    }

    @Override
    public LocalDateTime getStartDateTime() {

        return start;
    }

    @Override
    public void setStartDateTime(LocalDateTime start) {

        this.start = start;
    }

    @Override
    public Recurrence getRecurrence() {

        return recurrence;
    }

    @Override
    public void setRecurrence(@Nullable Recurrence recurrence) {

        this.recurrence = (recurrence != null) ? recurrence : Recurrence.NONE;
    }

}
