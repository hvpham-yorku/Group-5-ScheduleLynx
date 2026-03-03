package ca.yorku.eecs2311.schedulelynx.server.web.dto;

import ca.yorku.eecs2311.schedulelynx.server.domain.Weekday;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

import java.time.LocalTime;

public record EventRequest(

//      Annotation  Type        Name
        @Nullable   Long        id,
        @NotBlank   String      title,
        @NotNull    Weekday     day,
        @NotNull    LocalTime   start,
        @NotNull    LocalTime   end
) {
}