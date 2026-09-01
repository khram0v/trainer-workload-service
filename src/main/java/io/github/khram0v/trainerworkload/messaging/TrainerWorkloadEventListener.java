package io.github.khram0v.trainerworkload.messaging;

import io.github.khram0v.trainerworkload.dto.request.WorkloadEventRequest;
import io.github.khram0v.trainerworkload.service.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class TrainerWorkloadEventListener {

    private final TrainerWorkloadService trainerWorkloadService;
    private final ObjectMapper objectMapper;

    @JmsListener(destination = "${messaging.trainer-workload-events-queue}")
    public void onWorkloadEvent(String payload) {
        WorkloadEventRequest request = objectMapper.readValue(payload, WorkloadEventRequest.class);

        trainerWorkloadService.applyWorkloadEvent(request);

        log.debug("Consumed workload event for trainer '{}': {} {} min on {}",
                request.trainerUsername(), request.actionType(), request.trainingDuration(), request.trainingDate());
    }
}
