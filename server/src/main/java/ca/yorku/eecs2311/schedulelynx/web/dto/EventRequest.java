package ca.yorku.eecs2311.schedulelynx.web.dto;

import ca.yorku.eecs2311.schedulelynx.domain.events.Difficulty;
import ca.yorku.eecs2311.schedulelynx.domain.events.EventType;
import ca.yorku.eecs2311.schedulelynx.domain.events.Recurrence;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * The 'Data Transfer Object' for moving information from the frontend to the backend
 * for all event types. Combines {@link FixedEventRequest} and {@link TaskCreateRequest}
 * into one easy to implement record.
 *
 * @param type        (REQUIRED) an {@link EventType} enum stating under what category this event falls under.
 * @param name        (REQUIRED) whatever the user decided to call the event.
 * @param description (optional) the user's description of the event in detail.
 * @param start       (optional) the date and time of when the event begins.<br>
 *                    Format as "YYYY-MM-DDTHH:mm:ss".
 * @param end         (REQUIRED) the date and time of when the event ends.<br>
 *                    Format as "YYYY-MM-DDTHH:mm:ss".
 * @param recurrence  (optional) "NONE", "DAILY", "WEEKLY", "BIWEEKLY", or "MONTHLY"
 * @param estMinutes  (optional) how many minutes the user expects to devote their life to the task.
 * @param difficulty  (optional) the {@link Difficulty} that the user anticipates the event to be.
 * @author Eric Hanson
 */
public record EventRequest(
        @NotNull EventType type,
        @NotBlank String name,
        String description,
        LocalDateTime start,
        @NotNull LocalDateTime end,
        Recurrence recurrence,
        @Min(0) Integer estMinutes,
        Difficulty difficulty
) {}