package ca.yorku.eecs2311.schedulelynx.server.web.dto;

import ca.yorku.eecs2311.schedulelynx.server.domain.Weekday;

import java.time.LocalTime;

public record EventResponse(

//      Type        Name
        long        id,
        String      title,
        Weekday     day,
        LocalTime   start,
        LocalTime   end
) {}