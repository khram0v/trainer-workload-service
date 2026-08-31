package io.github.khram0v.trainerworkload.dto.response;

import java.util.List;

public record YearSummaryResponse(
        int year,
        List<MonthSummaryResponse> months
) {
}
