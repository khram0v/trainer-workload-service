package io.github.khram0v.trainerworkload.messaging;

import io.github.khram0v.trainerworkload.dto.request.ActionType;
import io.github.khram0v.trainerworkload.dto.request.WorkloadEventRequest;
import io.github.khram0v.trainerworkload.service.TrainerWorkloadService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadEventListenerTest {

    @Mock private TrainerWorkloadService trainerWorkloadService;
    @Mock private DeadLetterPublisher deadLetterPublisher;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    private TrainerWorkloadEventListener listener;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        listener = new TrainerWorkloadEventListener(
                trainerWorkloadService, objectMapper, validator, deadLetterPublisher);
    }

    @Test
    void onWorkloadEvent_whenValid_deserializesPayload_andDelegatesToService() {
        WorkloadEventRequest request = new WorkloadEventRequest(
                "Jane.Smith", "Jane", "Smith", true,
                LocalDate.of(2024, Month.JUNE, 10), 60, ActionType.ADD);
        String payload = objectMapper.writeValueAsString(request);

        listener.onWorkloadEvent(payload);

        verify(trainerWorkloadService).applyWorkloadEvent(request);
        verifyNoInteractions(deadLetterPublisher);
    }

    @Test
    void onWorkloadEvent_whenPayloadMalformed_publishesToDeadLetterQueue_andDoesNotCallService() {
        listener.onWorkloadEvent("not-json");

        verify(deadLetterPublisher).publish(eq("not-json"), any());
        verifyNoInteractions(trainerWorkloadService);
    }

    @Test
    void onWorkloadEvent_whenRequiredFieldMissing_publishesToDeadLetterQueue_withViolationReasons() {
        String payload = """
                {
                  "trainerUsername": "Jane.Smith",
                  "trainerFirstName": "Jane",
                  "trainerLastName": "Smith",
                  "active": true,
                  "trainingDate": "2024-06-10",
                  "trainingDuration": null,
                  "actionType": "ADD"
                }
                """;

        listener.onWorkloadEvent(payload);

        ArgumentCaptor<List<String>> reasonsCaptor = ArgumentCaptor.forClass(List.class);
        verify(deadLetterPublisher).publish(eq(payload), reasonsCaptor.capture());
        assertThat(reasonsCaptor.getValue()).anyMatch(reason -> reason.contains("trainingDuration"));
        verify(trainerWorkloadService, never()).applyWorkloadEvent(any());
    }

    @Test
    void onWorkloadEvent_whenTrainingDurationNonPositive_publishesToDeadLetterQueue() {
        WorkloadEventRequest invalidRequest = new WorkloadEventRequest(
                "Jane.Smith", "Jane", "Smith", true,
                LocalDate.of(2024, Month.JUNE, 10), 0, ActionType.ADD);
        String payload = objectMapper.writeValueAsString(invalidRequest);

        listener.onWorkloadEvent(payload);

        verify(deadLetterPublisher).publish(eq(payload), any());
        verifyNoInteractions(trainerWorkloadService);
    }
}
