package ca.yorku.eecs2311.schedulelynx.domain.events;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Contains the data for Lecture-type events.
 */
public class LectureEvent extends AbstractEvent {

    protected LocalDateTime start;
    protected Recurrence recurrence;
    protected Difficulty difficulty;

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
        super(id, type, name, desc, end);

        this.start = start;
        this.recurrence = (recurrence != null) ? recurrence : Recurrence.NONE;
        this.difficulty = (difficulty != null) ? difficulty : Difficulty.MEDIUM;

        if (type == EventType.LECTURE) return;
        throw new IllegalArgumentException("""
                Type != 'LECTURE'.
                Make sure you're calling the right class for:\s""" + type.name()
        );
    }

    /** @return the date and time of the start of the lecture. */
    public LocalDateTime getStartDateTime() {

        return start;
    }

    /** Overwrites the previous date and time of the lecture's start. */
    public void setStartDateTime(LocalDateTime start) {

        this.start = start;
    }

    /** @return the {@link Recurrence} of the lecture. */
    public Recurrence getRecurrence() {

        return recurrence;
    }

    /** Overwrites the previous {@link Recurrence} of the lecture.<br>
     *  If {@code null} provided, recurrence will be set to NONE. */
    public void setRecurrence(@Nullable Recurrence recurrence) {

        this.recurrence = (recurrence != null) ? recurrence : Recurrence.NONE;
    }

    /** @return the {@link Difficulty} of the lecture. */
    public Difficulty getDifficulty() {

        return difficulty;
    }

    /** Overwrites the previous difficulty level of the lecture. */
    public void setDifficulty(@NotNull Difficulty difficulty) {

        this.difficulty = difficulty;
    }

}
