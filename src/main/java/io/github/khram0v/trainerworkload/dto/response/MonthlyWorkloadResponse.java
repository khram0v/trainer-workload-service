package io.github.khram0v.trainerworkload.dto.response;

public record MonthlyWorkloadResponse(
        String trainerUsername,
        int year,
        int month,
        int trainingSummaryDuration
) {
}
