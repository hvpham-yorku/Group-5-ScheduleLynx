package ca.yorku.eecs2311.schedulelynx.domain.events;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

/** The generic event class containing fields common to all events.<br>
 *  All events should extend this class. */
public abstract class AbstractEvent {

    // private fields have no set method
    // and are effectively final
    private long id;
    private EventType type;

    // protected fields are changeable
    protected String name;
    protected String desc; // description
    protected LocalDateTime end;
    protected Difficulty diff;

    AbstractEvent() {}

    AbstractEvent(
                        long            id,
            @NotNull    EventType       type,
            @NotBlank   String          name,
            @Nullable   String          desc,
            @NotNull    LocalDateTime   end,
            @Nullable   Difficulty      diff)
    {
        this.id   =  id;
        this.type =  type;
        this.name =  name;
        this.end  =  end;
        this.desc = (desc != null) ? desc : "";
        this.diff = (diff != null) ? diff : Difficulty.MEDIUM;
    }

    /** @return the event's unique numerical identifier. */
    public long getID() {

        return id;
    }

    /** @return the {@link EventType} that this event is. */
    public EventType getType() {

        return type;
    }

    /** @return the name/title of the event. */
    public String getName() {

        return name;
    }

    /** Overwrites the previous name/title of the event. */
    public void setName(String name) {

        this.name = name;
    }

    /** @return the description of the event. */
    public String getDesc() {

        return desc;
    }

    /** Overwrites the previous description of the event. */
    public void setDesc(String desc) {

        this.desc = desc;
    }

    /** @return the date and time of the end of the event. */
    public LocalDateTime getEndDateTime() {

        return end;
    }

    /** Overwrites the previous date and time of the event's end. */
    public void setEndDateTime(LocalDateTime end) {

        this.end = end;
    }

    /** @return the {@link Difficulty} of the event. */
    Difficulty getDifficulty() {

        return diff;
    }

    /** Overwrites the previous difficulty level of the event. */
    void setDifficulty(@NotNull Difficulty difficulty) {

        this.diff = difficulty;
    }

}
