package io.github.khram0v.trainerworkload.controller;

import io.github.khram0v.trainerworkload.dto.request.WorkloadEventRequest;
import io.github.khram0v.trainerworkload.dto.response.MonthlyWorkloadResponse;
import io.github.khram0v.trainerworkload.dto.response.TrainerWorkloadSummaryResponse;
import io.github.khram0v.trainerworkload.service.TrainerWorkloadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/trainer-workloads")
@RequiredArgsConstructor
public class TrainerWorkloadController {

    private final TrainerWorkloadService trainerWorkloadService;

    @PostMapping
    public void applyWorkload(@Valid @RequestBody WorkloadEventRequest request) {
        trainerWorkloadService.applyWorkloadEvent(request);
    }

    @GetMapping("/{username}")
    public TrainerWorkloadSummaryResponse getSummary(@PathVariable String username) {
        return trainerWorkloadService.getSummary(username);
    }

    @GetMapping("/{username}/years/{year}/months/{month}")
    public MonthlyWorkloadResponse getMonthlyWorkload(@PathVariable String username,
                                                      @PathVariable int year,
                                                      @PathVariable int month) {
        return trainerWorkloadService.getMonthlyDuration(username, year, month);
    }
}
