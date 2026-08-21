package io.github.khram0v.trainerworkload.controller;

import io.github.khram0v.trainerworkload.dto.request.ActionType;
import io.github.khram0v.trainerworkload.dto.request.WorkloadEventRequest;
import io.github.khram0v.trainerworkload.dto.response.MonthSummaryResponse;
import io.github.khram0v.trainerworkload.dto.response.MonthlyWorkloadResponse;
import io.github.khram0v.trainerworkload.dto.response.TrainerWorkloadSummaryResponse;
import io.github.khram0v.trainerworkload.dto.response.YearSummaryResponse;
import io.github.khram0v.trainerworkload.exception.NotFoundException;
import io.github.khram0v.trainerworkload.service.TrainerWorkloadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TrainerWorkloadController.class)
@AutoConfigureMockMvc(addFilters = false)
class TrainerWorkloadControllerTest {

    @Autowired private MockMvc mockMvc;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @MockitoBean private TrainerWorkloadService trainerWorkloadService;

    // ~~~~~ applyWorkload ~~~~~

    @Test
    void applyWorkload_returns200_andUnpacksRequest() throws Exception {
        var request = new WorkloadEventRequest(
                "Jane.Smith", "Jane", "Smith", true,
                LocalDate.of(2024, Month.JUNE, 10), 60, ActionType.ADD);

        mockMvc.perform(post("/api/v1/trainer-workloads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainerWorkloadService).applyWorkloadEvent(request);
    }

    @Test
    void applyWorkload_whenBlankUsername_returns400_andDoesNotCallService() throws Exception {
        var request = new WorkloadEventRequest(
                "", "Jane", "Smith", true,
                LocalDate.of(2024, Month.JUNE, 10), 60, ActionType.ADD);

        mockMvc.perform(post("/api/v1/trainer-workloads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(trainerWorkloadService, never()).applyWorkloadEvent(any());
    }

    @Test
    void applyWorkload_whenNonPositiveDuration_returns400() throws Exception {
        var request = new WorkloadEventRequest(
                "Jane.Smith", "Jane", "Smith", true,
                LocalDate.of(2024, Month.JUNE, 10), 0, ActionType.ADD);

        mockMvc.perform(post("/api/v1/trainer-workloads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(trainerWorkloadService, never()).applyWorkloadEvent(any());
    }

    // ~~~~~ getSummary ~~~~~

    @Test
    void getSummary_returns200_andBody() throws Exception {
        var stub = new TrainerWorkloadSummaryResponse(
                "Jane.Smith", "Jane", "Smith", true,
                List.of(new YearSummaryResponse(2024, List.of(new MonthSummaryResponse(6, 60)))));
        when(trainerWorkloadService.getSummary("Jane.Smith")).thenReturn(stub);

        mockMvc.perform(get("/api/v1/trainer-workloads/{username}", "Jane.Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("Jane.Smith"))
                .andExpect(jsonPath("$.trainerStatus").value(true))
                .andExpect(jsonPath("$.years[0].year").value(2024))
                .andExpect(jsonPath("$.years[0].months[0].trainingSummaryDuration").value(60));
    }

    @Test
    void getSummary_whenNotFound_returns404() throws Exception {
        when(trainerWorkloadService.getSummary("Ghost"))
                .thenThrow(new NotFoundException("No workload data found for trainer: Ghost"));

        mockMvc.perform(get("/api/v1/trainer-workloads/{username}", "Ghost"))
                .andExpect(status().isNotFound());
    }

    // ~~~~~ getMonthlyWorkload ~~~~~

    @Test
    void getMonthlyWorkload_returns200_andBody() throws Exception {
        when(trainerWorkloadService.getMonthlyDuration("Jane.Smith", 2024, 6))
                .thenReturn(new MonthlyWorkloadResponse("Jane.Smith", 2024, 6, 90));

        mockMvc.perform(get("/api/v1/trainer-workloads/{username}/years/{year}/months/{month}",
                        "Jane.Smith", 2024, 6))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainingSummaryDuration").value(90));

        verify(trainerWorkloadService).getMonthlyDuration("Jane.Smith", 2024, 6);
    }

    @Test
    void getMonthlyWorkload_whenNotFound_returns404() throws Exception {
        when(trainerWorkloadService.getMonthlyDuration("Ghost", 2024, 6))
                .thenThrow(new NotFoundException("No workload data found for trainer: Ghost"));

        mockMvc.perform(get("/api/v1/trainer-workloads/{username}/years/{year}/months/{month}",
                        "Ghost", 2024, 6))
                .andExpect(status().isNotFound());
    }
}
