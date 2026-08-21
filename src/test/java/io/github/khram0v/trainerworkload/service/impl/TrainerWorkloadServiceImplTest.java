package io.github.khram0v.trainerworkload.service.impl;

import io.github.khram0v.trainerworkload.dto.request.ActionType;
import io.github.khram0v.trainerworkload.dto.request.WorkloadEventRequest;
import io.github.khram0v.trainerworkload.dto.response.MonthSummaryResponse;
import io.github.khram0v.trainerworkload.dto.response.MonthlyWorkloadResponse;
import io.github.khram0v.trainerworkload.dto.response.TrainerWorkloadSummaryResponse;
import io.github.khram0v.trainerworkload.dto.response.YearSummaryResponse;
import io.github.khram0v.trainerworkload.exception.NotFoundException;
import io.github.khram0v.trainerworkload.mapper.TrainerWorkloadMapper;
import io.github.khram0v.trainerworkload.model.TrainerWorkload;
import io.github.khram0v.trainerworkload.model.WorkloadMonth;
import io.github.khram0v.trainerworkload.model.WorkloadYear;
import io.github.khram0v.trainerworkload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceImplTest {

    @Mock private TrainerWorkloadRepository trainerWorkloadRepository;
    @Mock private TrainerWorkloadMapper trainerWorkloadMapper;

    @InjectMocks private TrainerWorkloadServiceImpl trainerWorkloadService;

    // ~~~~~ applyWorkloadEvent ~~~~~

    @Test
    void applyWorkloadEvent_whenNewTrainer_createsTrainerYearAndMonth_withAddedDuration() {
        when(trainerWorkloadRepository.findByUsernameWithYearsAndMonths("Jane.Smith"))
                .thenReturn(Optional.empty());
        when(trainerWorkloadRepository.save(any(TrainerWorkload.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        WorkloadEventRequest request = new WorkloadEventRequest(
                "Jane.Smith", "Jane", "Smith", true,
                LocalDate.of(2024, Month.JUNE, 10), 60, ActionType.ADD);

        trainerWorkloadService.applyWorkloadEvent(request);

        ArgumentCaptor<TrainerWorkload> captor = ArgumentCaptor.forClass(TrainerWorkload.class);
        verify(trainerWorkloadRepository).save(captor.capture());
        TrainerWorkload saved = captor.getValue();

        assertThat(saved.getUsername()).isEqualTo("Jane.Smith");
        assertThat(saved.getFirstName()).isEqualTo("Jane");
        assertThat(saved.getLastName()).isEqualTo("Smith");
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getYears()).hasSize(1);
        WorkloadYear year = saved.getYears().iterator().next();
        assertThat(year.getYear()).isEqualTo(2024);
        assertThat(year.getMonths()).hasSize(1);
        WorkloadMonth month = year.getMonths().iterator().next();
        assertThat(month.getMonth()).isEqualTo(6);
        assertThat(month.getTrainingSummaryDuration()).isEqualTo(60);
    }

    @Test
    void applyWorkloadEvent_whenExistingTrainerSameMonth_accumulatesAddedDuration() {
        TrainerWorkload existing = new TrainerWorkload("Jane.Smith", "Jane", "Smith", true);
        WorkloadYear year = new WorkloadYear(existing, 2024);
        WorkloadMonth month = new WorkloadMonth(year, 6);
        month.setTrainingSummaryDuration(60);
        year.getMonths().add(month);
        existing.getYears().add(year);

        when(trainerWorkloadRepository.findByUsernameWithYearsAndMonths("Jane.Smith"))
                .thenReturn(Optional.of(existing));
        when(trainerWorkloadRepository.save(any(TrainerWorkload.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        WorkloadEventRequest request = new WorkloadEventRequest(
                "Jane.Smith", "Jane", "Smith", true,
                LocalDate.of(2024, Month.JUNE, 20), 30, ActionType.ADD);

        trainerWorkloadService.applyWorkloadEvent(request);

        assertThat(month.getTrainingSummaryDuration()).isEqualTo(90);
        assertThat(existing.getYears()).hasSize(1);
    }

    @Test
    void applyWorkloadEvent_whenDeleteAction_subtractsDuration() {
        TrainerWorkload existing = new TrainerWorkload("Jane.Smith", "Jane", "Smith", true);
        WorkloadYear year = new WorkloadYear(existing, 2024);
        WorkloadMonth month = new WorkloadMonth(year, 6);
        month.setTrainingSummaryDuration(90);
        year.getMonths().add(month);
        existing.getYears().add(year);

        when(trainerWorkloadRepository.findByUsernameWithYearsAndMonths("Jane.Smith"))
                .thenReturn(Optional.of(existing));
        when(trainerWorkloadRepository.save(any(TrainerWorkload.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        WorkloadEventRequest request = new WorkloadEventRequest(
                "Jane.Smith", "Jane", "Smith", true,
                LocalDate.of(2024, Month.JUNE, 20), 30, ActionType.DELETE);

        trainerWorkloadService.applyWorkloadEvent(request);

        assertThat(month.getTrainingSummaryDuration()).isEqualTo(60);
    }

    @Test
    void applyWorkloadEvent_whenDeleteExceedsExistingDuration_clampsAtZero() {
        TrainerWorkload existing = new TrainerWorkload("Jane.Smith", "Jane", "Smith", true);
        WorkloadYear year = new WorkloadYear(existing, 2024);
        WorkloadMonth month = new WorkloadMonth(year, 6);
        month.setTrainingSummaryDuration(20);
        year.getMonths().add(month);
        existing.getYears().add(year);

        when(trainerWorkloadRepository.findByUsernameWithYearsAndMonths("Jane.Smith"))
                .thenReturn(Optional.of(existing));
        when(trainerWorkloadRepository.save(any(TrainerWorkload.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        WorkloadEventRequest request = new WorkloadEventRequest(
                "Jane.Smith", "Jane", "Smith", true,
                LocalDate.of(2024, Month.JUNE, 20), 60, ActionType.DELETE);

        trainerWorkloadService.applyWorkloadEvent(request);

        assertThat(month.getTrainingSummaryDuration()).isZero();
    }

    @Test
    void applyWorkloadEvent_whenDifferentMonthSameYear_createsSeparateMonthEntry() {
        TrainerWorkload existing = new TrainerWorkload("Jane.Smith", "Jane", "Smith", true);
        WorkloadYear year = new WorkloadYear(existing, 2024);
        WorkloadMonth june = new WorkloadMonth(year, 6);
        june.setTrainingSummaryDuration(60);
        year.getMonths().add(june);
        existing.getYears().add(year);

        when(trainerWorkloadRepository.findByUsernameWithYearsAndMonths("Jane.Smith"))
                .thenReturn(Optional.of(existing));
        when(trainerWorkloadRepository.save(any(TrainerWorkload.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        WorkloadEventRequest request = new WorkloadEventRequest(
                "Jane.Smith", "Jane", "Smith", true,
                LocalDate.of(2024, Month.JULY, 1), 45, ActionType.ADD);

        trainerWorkloadService.applyWorkloadEvent(request);

        assertThat(year.getMonths()).hasSize(2);
        assertThat(june.getTrainingSummaryDuration()).isEqualTo(60);
    }

    @Test
    void applyWorkloadEvent_updatesTrainerNameAndActiveStatusEachTime() {
        TrainerWorkload existing = new TrainerWorkload("Jane.Smith", "Old", "Name", false);

        when(trainerWorkloadRepository.findByUsernameWithYearsAndMonths("Jane.Smith"))
                .thenReturn(Optional.of(existing));
        when(trainerWorkloadRepository.save(any(TrainerWorkload.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        WorkloadEventRequest request = new WorkloadEventRequest(
                "Jane.Smith", "Jane", "Smith", true,
                LocalDate.of(2024, Month.JUNE, 20), 30, ActionType.ADD);

        trainerWorkloadService.applyWorkloadEvent(request);

        assertThat(existing.getFirstName()).isEqualTo("Jane");
        assertThat(existing.getLastName()).isEqualTo("Smith");
        assertThat(existing.isActive()).isTrue();
    }

    // ~~~~~ getSummary ~~~~~

    @Test
    void getSummary_whenTrainerExists_mapsAndReturns() {
        TrainerWorkload trainerWorkload = new TrainerWorkload("Jane.Smith", "Jane", "Smith", true);
        TrainerWorkloadSummaryResponse stub = new TrainerWorkloadSummaryResponse(
                "Jane.Smith", "Jane", "Smith", true,
                List.of(new YearSummaryResponse(2024, List.of(new MonthSummaryResponse(6, 60)))));

        when(trainerWorkloadRepository.findByUsernameWithYearsAndMonths("Jane.Smith"))
                .thenReturn(Optional.of(trainerWorkload));
        when(trainerWorkloadMapper.toSummaryResponse(trainerWorkload)).thenReturn(stub);

        TrainerWorkloadSummaryResponse result = trainerWorkloadService.getSummary("Jane.Smith");

        assertThat(result).isSameAs(stub);
    }

    @Test
    void getSummary_whenTrainerNotFound_throws() {
        when(trainerWorkloadRepository.findByUsernameWithYearsAndMonths("Ghost"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerWorkloadService.getSummary("Ghost"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Ghost");
    }

    // ~~~~~ getMonthlyDuration ~~~~~

    @Test
    void getMonthlyDuration_whenMonthExists_returnsDuration() {
        TrainerWorkload trainerWorkload = new TrainerWorkload("Jane.Smith", "Jane", "Smith", true);
        WorkloadYear year = new WorkloadYear(trainerWorkload, 2024);
        WorkloadMonth month = new WorkloadMonth(year, 6);
        month.setTrainingSummaryDuration(90);
        year.getMonths().add(month);
        trainerWorkload.getYears().add(year);

        when(trainerWorkloadRepository.findByUsernameWithYearsAndMonths("Jane.Smith"))
                .thenReturn(Optional.of(trainerWorkload));

        MonthlyWorkloadResponse result = trainerWorkloadService.getMonthlyDuration("Jane.Smith", 2024, 6);

        assertThat(result).isEqualTo(new MonthlyWorkloadResponse("Jane.Smith", 2024, 6, 90));
    }

    @Test
    void getMonthlyDuration_whenMonthHasNoRecordedTraining_returnsZero() {
        TrainerWorkload trainerWorkload = new TrainerWorkload("Jane.Smith", "Jane", "Smith", true);

        when(trainerWorkloadRepository.findByUsernameWithYearsAndMonths("Jane.Smith"))
                .thenReturn(Optional.of(trainerWorkload));

        MonthlyWorkloadResponse result = trainerWorkloadService.getMonthlyDuration("Jane.Smith", 2024, 6);

        assertThat(result).isEqualTo(new MonthlyWorkloadResponse("Jane.Smith", 2024, 6, 0));
    }

    @Test
    void getMonthlyDuration_whenTrainerNotFound_throws() {
        when(trainerWorkloadRepository.findByUsernameWithYearsAndMonths("Ghost"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerWorkloadService.getMonthlyDuration("Ghost", 2024, 6))
                .isInstanceOf(NotFoundException.class);
    }
}
