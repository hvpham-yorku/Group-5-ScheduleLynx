package ca.yorku.eecs2311.schedulelynx.web.dto;

import ca.yorku.eecs2311.schedulelynx.domain.events.Difficulty;
import ca.yorku.eecs2311.schedulelynx.domain.events.EventType;
import ca.yorku.eecs2311.schedulelynx.domain.events.Recurrence;
import ca.yorku.eecs2311.schedulelynx.domain.Weekday;
import ca.yorku.eecs2311.schedulelynx.web.controller.EventController;

import java.time.LocalDateTime;

/**
 * @param id a unique number representing the specific event.
 * @author Eric Hanson
 * @see EventController for other parameters' documentation.
 */
public record EventResponse(
        long id,
        EventType type,
        String title,
        Weekday day,
        LocalDateTime start,
        LocalDateTime end,
        Recurrence recurrence,
        int estMinutes,
        Difficulty difficulty
) {}