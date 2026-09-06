package io.github.khram0v.trainerworkload.service.impl;

import io.github.khram0v.trainerworkload.dto.request.ActionType;
import io.github.khram0v.trainerworkload.dto.request.WorkloadEventRequest;
import io.github.khram0v.trainerworkload.dto.response.MonthlyWorkloadResponse;
import io.github.khram0v.trainerworkload.dto.response.TrainerWorkloadSummaryResponse;
import io.github.khram0v.trainerworkload.exception.NotFoundException;
import io.github.khram0v.trainerworkload.mapper.TrainerWorkloadMapper;
import io.github.khram0v.trainerworkload.model.TrainerWorkload;
import io.github.khram0v.trainerworkload.model.WorkloadMonth;
import io.github.khram0v.trainerworkload.model.WorkloadYear;
import io.github.khram0v.trainerworkload.repository.TrainerWorkloadRepository;
import io.github.khram0v.trainerworkload.service.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainerWorkloadServiceImpl implements TrainerWorkloadService {

    private final TrainerWorkloadRepository trainerWorkloadRepository;
    private final TrainerWorkloadMapper trainerWorkloadMapper;

    @Override
    public void applyWorkloadEvent(WorkloadEventRequest request) {
        TrainerWorkload trainerWorkload = trainerWorkloadRepository.findByUsername(request.trainerUsername())
                .orElseGet(() -> {
                    log.debug("No existing workload record for trainer '{}', creating a new one",
                            request.trainerUsername());
                    return new TrainerWorkload(
                            request.trainerUsername(), request.trainerFirstName(),
                            request.trainerLastName(), request.active());
                });

        trainerWorkload.setFirstName(request.trainerFirstName());
        trainerWorkload.setLastName(request.trainerLastName());
        trainerWorkload.setActive(request.active());

        int year = request.trainingDate().getYear();
        int month = request.trainingDate().getMonthValue();

        WorkloadYear workloadYear = findOrCreateYear(trainerWorkload, year);
        WorkloadMonth workloadMonth = findOrCreateMonth(workloadYear, month);

        int delta = request.actionType() == ActionType.ADD
                ? request.trainingDuration()
                : -request.trainingDuration();
        workloadMonth.setTrainingSummaryDuration(
                Math.max(0, workloadMonth.getTrainingSummaryDuration() + delta));

        trainerWorkloadRepository.save(trainerWorkload);
        log.info("Applied {} of {} for trainer '{}' on {}-{}: new monthly total is {}",
                request.actionType(), request.trainingDuration(), request.trainerUsername(),
                year, month, workloadMonth.getTrainingSummaryDuration());
    }

    @Override
    public TrainerWorkloadSummaryResponse getSummary(String username) {
        TrainerWorkload trainerWorkload = trainerWorkloadRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("No workload data found for trainer: " + username));
        log.debug("Retrieved workload summary for trainer '{}': {} year(s) on record",
                username, trainerWorkload.getYears().size());
        return trainerWorkloadMapper.toSummaryResponse(trainerWorkload);
    }

    @Override
    public MonthlyWorkloadResponse getMonthlyDuration(String username, int year, int month) {
        TrainerWorkload trainerWorkload = trainerWorkloadRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("No workload data found for trainer: " + username));

        int duration = trainerWorkload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .flatMap(y -> y.getMonths().stream())
                .filter(m -> m.getMonth() == month)
                .mapToInt(WorkloadMonth::getTrainingSummaryDuration)
                .findFirst()
                .orElse(0);

        log.debug("Retrieved monthly workload for trainer '{}' {}-{}: {} min", username, year, month, duration);
        return new MonthlyWorkloadResponse(username, year, month, duration);
    }

    private WorkloadYear findOrCreateYear(TrainerWorkload trainerWorkload, int year) {
        return trainerWorkload.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    log.debug("No record for year {} for trainer '{}', creating a new one",
                            year, trainerWorkload.getUsername());
                    WorkloadYear newYear = new WorkloadYear(year);
                    trainerWorkload.getYears().add(newYear);
                    return newYear;
                });
    }

    private WorkloadMonth findOrCreateMonth(WorkloadYear workloadYear, int month) {
        return workloadYear.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    log.debug("No record for month {} in year {}, creating a new one",
                            month, workloadYear.getYear());
                    WorkloadMonth newMonth = new WorkloadMonth(month);
                    workloadYear.getMonths().add(newMonth);
                    return newMonth;
                });
    }
}
