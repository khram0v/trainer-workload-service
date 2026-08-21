package io.github.khram0v.trainerworkload.dto.response;

public record MonthSummaryResponse(
        int month,
        int trainingSummaryDuration
) {
}
