package io.github.khram0v.trainerworkload.dto.response;

import java.util.List;

public record TrainerWorkloadSummaryResponse(
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        boolean trainerStatus,
        List<YearSummaryResponse> years
) {
}
