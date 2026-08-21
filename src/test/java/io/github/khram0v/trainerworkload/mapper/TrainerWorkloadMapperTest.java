package io.github.khram0v.trainerworkload.mapper;

import io.github.khram0v.trainerworkload.dto.response.TrainerWorkloadSummaryResponse;
import io.github.khram0v.trainerworkload.dto.response.YearSummaryResponse;
import io.github.khram0v.trainerworkload.model.TrainerWorkload;
import io.github.khram0v.trainerworkload.model.WorkloadMonth;
import io.github.khram0v.trainerworkload.model.WorkloadYear;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrainerWorkloadMapperTest {

    private final TrainerWorkloadMapper mapper = new TrainerWorkloadMapper();

    @Test
    void toSummaryResponse_mapsFields_andSortsYearsAndMonths() {
        TrainerWorkload trainerWorkload = new TrainerWorkload("Jane.Smith", "Jane", "Smith", true);

        WorkloadYear year2025 = new WorkloadYear(trainerWorkload, 2025);
        WorkloadMonth jan2025 = new WorkloadMonth(year2025, 1);
        jan2025.setTrainingSummaryDuration(30);
        year2025.getMonths().add(jan2025);

        WorkloadYear year2024 = new WorkloadYear(trainerWorkload, 2024);
        WorkloadMonth june2024 = new WorkloadMonth(year2024, 6);
        june2024.setTrainingSummaryDuration(60);
        WorkloadMonth march2024 = new WorkloadMonth(year2024, 3);
        march2024.setTrainingSummaryDuration(45);
        year2024.getMonths().add(june2024);
        year2024.getMonths().add(march2024);

        trainerWorkload.getYears().add(year2025);
        trainerWorkload.getYears().add(year2024);

        TrainerWorkloadSummaryResponse response = mapper.toSummaryResponse(trainerWorkload);

        assertThat(response.trainerUsername()).isEqualTo("Jane.Smith");
        assertThat(response.trainerFirstName()).isEqualTo("Jane");
        assertThat(response.trainerLastName()).isEqualTo("Smith");
        assertThat(response.trainerStatus()).isTrue();

        assertThat(response.years()).extracting(YearSummaryResponse::year)
                .containsExactly(2024, 2025);

        YearSummaryResponse year2024Response = response.years().getFirst();
        assertThat(year2024Response.months()).extracting("month")
                .containsExactly(3, 6);
        assertThat(year2024Response.months()).extracting("trainingSummaryDuration")
                .containsExactly(45, 60);
    }

    @Test
    void toSummaryResponse_whenNoYears_returnsEmptyYearsList() {
        TrainerWorkload trainerWorkload = new TrainerWorkload("Jane.Smith", "Jane", "Smith", false);

        TrainerWorkloadSummaryResponse response = mapper.toSummaryResponse(trainerWorkload);

        assertThat(response.years()).isEmpty();
        assertThat(response.trainerStatus()).isFalse();
    }
}
