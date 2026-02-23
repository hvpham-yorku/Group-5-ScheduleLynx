package ca.yorku.eecs2311.schedulelynx.domain.events;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

/** Contains the data for Lab-type events. */
public class LaboratoryEvent extends AbstractEvent
        implements StartableEvent, RecurrableEvent {

    protected LocalDateTime start;
    protected Recurrence recurrence;

    public LaboratoryEvent() {}

    public LaboratoryEvent(
                        long            id,
            @NotNull    EventType       type,
            @NotBlank   String          name,
            @Nullable   String          desc,
            @NotNull    LocalDateTime   start,
            @NotNull    LocalDateTime   end,
            @Nullable   Recurrence      recurrence,
            @Nullable   Difficulty      difficulty)
    {
        super(id, type, name, desc, end, difficulty);

        this.start = start;
        this.recurrence = (recurrence != null) ? recurrence : Recurrence.NONE;

        if (type == EventType.LABORATORY) return;
        throw new IllegalArgumentException("""
                Type != 'LABORATORY'.
                Make sure you're calling the right class for:\s""" + type.name()
        );
    }

    @Override
    public LocalDateTime getStartDateTime() {

        return start;
    }

    @Override
    public void setStartDateTime(@NotNull LocalDateTime start) {

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
