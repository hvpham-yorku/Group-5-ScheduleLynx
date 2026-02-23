package ca.yorku.eecs2311.schedulelynx.web.dto;

import ca.yorku.eecs2311.schedulelynx.domain.Weekday;

import java.time.LocalTime;

public record FixedEventResponse(

//      Type        Name
        long        id,
        String      title,
        Weekday     day,
        LocalTime   start,
        LocalTime   end
) {}