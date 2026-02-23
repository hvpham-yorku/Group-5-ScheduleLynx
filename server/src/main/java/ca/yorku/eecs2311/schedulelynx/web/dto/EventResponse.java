package ca.yorku.eecs2311.schedulelynx.web.dto;

import ca.yorku.eecs2311.schedulelynx.domain.events.Difficulty;
import ca.yorku.eecs2311.schedulelynx.domain.events.EventType;
import ca.yorku.eecs2311.schedulelynx.domain.events.Recurrence;
import ca.yorku.eecs2311.schedulelynx.web.controller.EventController;

import java.time.LocalDateTime;

/**
 * @param id a unique number representing the specific event.
 * @see EventController for other parameters' documentation.
 */
public record EventResponse(
        long            id,
        EventType       type,
        String          name,
        String          description,
        LocalDateTime   start,
        LocalDateTime   end,
        Recurrence      recurrence,
        int             estMinutes,
        Difficulty      difficulty