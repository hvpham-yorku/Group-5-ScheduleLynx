package ca.yorku.eecs2311.schedulelynx.web.dto;

import ca.yorku.eecs2311.schedulelynx.domain.Difficulty;
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
        String title,
        Weekday day,
        LocalDateTime start,
        LocalDateTime end,
        String recurrence,
        int estMinutes,
        Difficulty difficulty
) {}