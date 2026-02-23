package ca.yorku.eecs2311.schedulelynx.domain.events;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

public class ExamEvent extends AbstractEvent
        implements StartableEvent, TimeableEvent {

    protected LocalDateTime start;
    protected int estimatedStudyTime;

    public ExamEvent() {}

    public ExamEvent(
            long id,
            @NotNull EventType type,
            @NotBlank String name,
            @Nullable String desc,
            @NotNull LocalDateTime start,
            @NotNull LocalDateTime end,
            @Nullable Integer estimatedStudyTime,
            @Nullable Difficulty diff)
    {
        super(id, type, name, desc, end, diff);

        this.start = start;
        this.estimatedStudyTime = (estimatedStudyTime != null)
                                ? estimatedStudyTime : 0;

        if (type == EventType.EXAM) return;
        throw new IllegalArgumentException("""
                Type != 'EXAM'.
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
    public int getEstimatedTime() {

        return estimatedStudyTime;
    }

    @Override
    public void setEstimatedTime(int minutes) {

        this.estimatedStudyTime = minutes;
    }

}
