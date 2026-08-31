package io.github.khram0v.trainerworkload.mapper;

import io.github.khram0v.trainerworkload.dto.response.MonthSummaryResponse;
import io.github.khram0v.trainerworkload.dto.response.TrainerWorkloadSummaryResponse;
import io.github.khram0v.trainerworkload.dto.response.YearSummaryResponse;
import io.github.khram0v.trainerworkload.model.TrainerWorkload;
import io.github.khram0v.trainerworkload.model.WorkloadMonth;
import io.github.khram0v.trainerworkload.model.WorkloadYear;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class TrainerWorkloadMapper {

    public TrainerWorkloadSummaryResponse toSummaryResponse(TrainerWorkload trainerWorkload) {
        List<YearSummaryResponse> years = trainerWorkload.getYears().stream()
                .sorted(Comparator.comparingInt(WorkloadYear::getYear))
                .map(this::toYearSummary)
                .toList();

        return new TrainerWorkloadSummaryResponse(
                trainerWorkload.getUsername(),
                trainerWorkload.getFirstName(),
                trainerWorkload.getLastName(),
                trainerWorkload.isActive(),
                years);
    }

    private YearSummaryResponse toYearSummary(WorkloadYear workloadYear) {
        List<MonthSummaryResponse> months = workloadYear.getMonths().stream()
                .sorted(Comparator.comparingInt(WorkloadMonth::getMonth))
                .map(m -> new MonthSummaryResponse(m.getMonth(), m.getTrainingSummaryDuration()))
                .toList();
        return new YearSummaryResponse(workloadYear.getYear(), months);
    }
}
