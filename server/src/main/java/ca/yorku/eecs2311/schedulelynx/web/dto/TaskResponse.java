package ca.yorku.eecs2311.schedulelynx.web.dto;

import ca.yorku.eecs2311.schedulelynx.domain.Difficulty;

import java.time.LocalDate;

public record TaskResponse(

//      Type        Name
        long        id,
        String      title,
        LocalDate   dueDate,
        int         estimatedHours,
        Difficulty  difficulty

) {
}