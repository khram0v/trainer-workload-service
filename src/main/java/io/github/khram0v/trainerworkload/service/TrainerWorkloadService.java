package io.github.khram0v.trainerworkload.service;

import io.github.khram0v.trainerworkload.dto.request.WorkloadEventRequest;
import io.github.khram0v.trainerworkload.dto.response.MonthlyWorkloadResponse;
import io.github.khram0v.trainerworkload.dto.response.TrainerWorkloadSummaryResponse;

public interface TrainerWorkloadService {

    void applyWorkloadEvent(WorkloadEventRequest request);

    TrainerWorkloadSummaryResponse getSummary(String username);

    MonthlyWorkloadResponse getMonthlyDuration(String username, int year, int month);
}
