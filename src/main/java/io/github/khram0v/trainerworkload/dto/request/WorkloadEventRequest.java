package io.github.khram0v.trainerworkload.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record WorkloadEventRequest(
        @NotBlank String trainerUsername,
        @NotBlank String trainerFirstName,
        @NotBlank String trainerLastName,
        @NotNull Boolean active,
        @NotNull LocalDate trainingDate,
        @NotNull @Positive Integer trainingDuration,
        @NotNull ActionType actionType
) {
}
