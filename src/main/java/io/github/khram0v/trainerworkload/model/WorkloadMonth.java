package io.github.khram0v.trainerworkload.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkloadMonth {

    @Min(1)
    @Max(12)
    private int month;

    @PositiveOrZero
    private int trainingSummaryDuration;

    public WorkloadMonth(int month) {
        this.month = month;
        this.trainingSummaryDuration = 0;
    }
}
