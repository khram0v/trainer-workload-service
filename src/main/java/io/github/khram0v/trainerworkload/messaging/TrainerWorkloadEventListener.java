package io.github.khram0v.trainerworkload.messaging;

import io.github.khram0v.trainerworkload.dto.request.WorkloadEventRequest;
import io.github.khram0v.trainerworkload.service.TrainerWorkloadService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class TrainerWorkloadEventListener {

    private static final String TRANSACTION_ID = "transactionId";

    private final TrainerWorkloadService trainerWorkloadService;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final DeadLetterPublisher deadLetterPublisher;

    @JmsListener(destination = "${messaging.trainer-workload-events-queue}")
    public void onWorkloadEvent(String payload,
                                @Header(value = TRANSACTION_ID, required = false) String transactionId) {
        if (transactionId != null) {
            MDC.put(TRANSACTION_ID, transactionId);
        }
        try {
            process(payload);
        } finally {
            MDC.remove(TRANSACTION_ID);
        }
    }

    private void process(String payload) {
        WorkloadEventRequest request;
        try {
            request = objectMapper.readValue(payload, WorkloadEventRequest.class);
        } catch (RuntimeException e) {
            log.warn("Rejecting malformed workload event payload: {}", e.getMessage());
            deadLetterPublisher.publish(payload, List.of("Malformed JSON payload: " + e.getMessage()));
            return;
        }

        List<String> violations = validate(request);
        if (!violations.isEmpty()) {
            log.warn("Rejecting invalid workload event for trainer '{}': {}",
                    request.trainerUsername(), violations);
            deadLetterPublisher.publish(payload, violations);
            return;
        }

        trainerWorkloadService.applyWorkloadEvent(request);
        log.debug("Consumed workload event for trainer '{}': {} {} min on {}",
                request.trainerUsername(), request.actionType(), request.trainingDuration(), request.trainingDate());
    }

    private List<String> validate(WorkloadEventRequest request) {
        Set<ConstraintViolation<WorkloadEventRequest>> violations = validator.validate(request);
        return violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .sorted()
                .toList();
    }
}
