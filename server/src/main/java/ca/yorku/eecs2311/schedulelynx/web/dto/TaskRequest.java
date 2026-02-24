package ca.yorku.eecs2311.schedulelynx.web.dto;

import ca.yorku.eecs2311.schedulelynx.domain.Difficulty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

public record TaskRequest(

//      Annotation  Type        Name
        @Nullable   Long        id,
        @NotBlank   String      title,
        @NotNull    LocalDate   dueDate,
        @Min(1)     int         estimatedHours,
        @NotNull    Difficulty  difficulty
) {
}
