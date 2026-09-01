package io.github.khram0v.trainerworkload.messaging;

import io.github.khram0v.trainerworkload.dto.request.ActionType;
import io.github.khram0v.trainerworkload.dto.request.WorkloadEventRequest;
import io.github.khram0v.trainerworkload.service.TrainerWorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadEventListenerTest {

    @Mock private TrainerWorkloadService trainerWorkloadService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private TrainerWorkloadEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new TrainerWorkloadEventListener(trainerWorkloadService, objectMapper);
    }

    @Test
    void onWorkloadEvent_deserializesPayload_andDelegatesToService() {
        WorkloadEventRequest request = new WorkloadEventRequest(
                "Jane.Smith", "Jane", "Smith", true,
                LocalDate.of(2024, Month.JUNE, 10), 60, ActionType.ADD);
        String payload = objectMapper.writeValueAsString(request);

        listener.onWorkloadEvent(payload);

        verify(trainerWorkloadService).applyWorkloadEvent(request);
    }

    @Test
    void onWorkloadEvent_whenPayloadMalformed_propagatesException() {
        assertThatThrownBy(() -> listener.onWorkloadEvent("not-json"))
                .isInstanceOf(RuntimeException.class);
    }
}
