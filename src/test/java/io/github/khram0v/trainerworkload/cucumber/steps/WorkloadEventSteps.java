package io.github.khram0v.trainerworkload.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.github.khram0v.trainerworkload.messaging.MessagingProperties;
import io.github.khram0v.trainerworkload.testsupport.ApiClient;
import io.github.khram0v.trainerworkload.testsupport.ScenarioContext;
import io.github.khram0v.trainerworkload.testsupport.ServiceTokenTestFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@RequiredArgsConstructor
public class WorkloadEventSteps {

    private static final LocalDate FIXED_TRAINING_DATE = LocalDate.of(2024, Month.JUNE, 10);

    private final JmsTemplate jmsTemplate;
    private final MessagingProperties messagingProperties;
    private final ObjectMapper objectMapper;
    private final ApiClient apiClient;
    private final ScenarioContext scenarioContext;

    @Given("a unique trainer username")
    public void aUniqueTrainerUsername() {
        scenarioContext.setTrainerUsername("Jane.Smith." + System.nanoTime());
    }

    @Given("an ADD workload event of {int} minutes is published for the trainer")
    public void anAddWorkloadEventIsPublished(int duration) {
        publishEvent("ADD", duration);
    }

    @Given("a DELETE workload event of {int} minutes is published for the trainer")
    public void aDeleteWorkloadEventIsPublished(int duration) {
        publishEvent("DELETE", duration);
    }

    @Given("a malformed workload event is published for the trainer")
    public void aMalformedWorkloadEventIsPublished() {
        jmsTemplate.send(messagingProperties.trainerWorkloadEventsQueue(),
                session -> session.createTextMessage("not-json"));
    }

    @Given("a workload event with a blank trainer username is published")
    public void aWorkloadEventWithBlankTrainerUsernameIsPublished() {
        send(eventBody("", "Jane", "Smith", true, FIXED_TRAINING_DATE, 60, "ADD"));
    }

    @Given("a workload event with a non-positive duration is published for the trainer")
    public void aWorkloadEventWithNonPositiveDurationIsPublished() {
        send(eventBody(scenarioContext.getTrainerUsername(), "Jane", "Smith", true, FIXED_TRAINING_DATE, 0, "ADD"));
    }

    @Then("the trainer's workload for that month eventually shows {int} minutes")
    public void theTrainersWorkloadForThatMonthEventuallyShows(int expectedDuration) {
        String trainerUsername = scenarioContext.getTrainerUsername();
        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            HttpResponse<String> response = fetchMonthlyWorkload(trainerUsername);
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(apiClient.json(response).get("trainingSummaryDuration").asInt())
                    .isEqualTo(expectedDuration);
        });
    }

    private void publishEvent(String actionType, int duration) {
        send(eventBody(scenarioContext.getTrainerUsername(), "Jane", "Smith",
                true, FIXED_TRAINING_DATE, duration, actionType));
    }

    private Map<String, Object> eventBody(String trainerUsername, String firstName, String lastName,
                                          boolean active, LocalDate date, int duration, String actionType) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("trainerUsername", trainerUsername);
        body.put("trainerFirstName", firstName);
        body.put("trainerLastName", lastName);
        body.put("active", active);
        body.put("trainingDate", date.toString());
        body.put("trainingDuration", duration);
        body.put("actionType", actionType);
        return body;
    }

    private void send(Map<String, Object> body) {
        String payload = objectMapper.writeValueAsString(body);
        jmsTemplate.send(messagingProperties.trainerWorkloadEventsQueue(),
                session -> session.createTextMessage(payload));
    }

    private HttpResponse<String> fetchMonthlyWorkload(String trainerUsername) {
        String token = ServiceTokenTestFactory.validServiceToken("gym-crm-service");
        return apiClient.get("/api/v1/trainer-workloads/" + trainerUsername
                + "/years/" + FIXED_TRAINING_DATE.getYear() + "/months/" + FIXED_TRAINING_DATE.getMonthValue(), token);
    }
}
