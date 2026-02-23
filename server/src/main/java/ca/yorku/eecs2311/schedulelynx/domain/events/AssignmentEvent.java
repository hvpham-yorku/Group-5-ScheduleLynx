package ca.yorku.eecs2311.schedulelynx.domain.events;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

public class AssignmentEvent extends AbstractEvent
        implements TimeableEvent {

    protected int estCompletionTime;

    public AssignmentEvent() {}

    public AssignmentEvent(
            long id,
            @NotNull EventType type,
            @NotBlank String name,
            @Nullable String desc,
            @NotNull LocalDateTime end,
            @Nullable Difficulty diff,
            int estCompletionTime)
    {
        super(id, type, name, desc, end, diff);
        this.estCompletionTime = estCompletionTime;
    }

    @Override
    public int getEstimatedTime() {

        return estCompletionTime;
    }

    @Override
    public void setEstimatedTime(int minutes) {

        this.estCompletionTime = minutes;
    }
}
