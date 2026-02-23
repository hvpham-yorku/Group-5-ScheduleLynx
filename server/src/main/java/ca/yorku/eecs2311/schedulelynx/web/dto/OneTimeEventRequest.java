package ca.yorku.eecs2311.schedulelynx.web.dto;

import ca.yorku.eecs2311.schedulelynx.domain.Weekday;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record OneTimeEventRequest(

//      Annotation  Type        Name
        @NotBlank   String      title,
        @NotNull    Weekday     day,
        @NotNull    LocalTime   start,
        @NotNull    LocalTime   end
) {
}