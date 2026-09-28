package io.github.khram0v.trainerworkload.cucumber.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.khram0v.trainerworkload.testsupport.ApiClient;
import io.github.khram0v.trainerworkload.testsupport.ScenarioContext;
import io.github.khram0v.trainerworkload.testsupport.ServiceTokenTestFactory;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class TrainerWorkloadSummarySteps {

    private final ApiClient apiClient;
    private final ScenarioContext scenarioContext;

    private String token;

    @When("I request the full workload summary for the trainer")
    public void requestFullWorkloadSummaryForTheTrainer() {
        scenarioContext.setLastResponse(apiClient.get(
                "/api/v1/trainer-workloads/" + scenarioContext.getTrainerUsername(), serviceToken()));
    }

    @When("I request the monthly workload for the trainer for year {int} and month {int}")
    public void requestMonthlyWorkloadForTheTrainer(int year, int month) {
        scenarioContext.setLastResponse(apiClient.get(
                "/api/v1/trainer-workloads/" + scenarioContext.getTrainerUsername()
                        + "/years/" + year + "/months/" + month, serviceToken()));
    }

    @When("I request the monthly workload for {string} for year {int} and month {int}")
    public void requestMonthlyWorkloadFor(String username, int year, int month) {
        scenarioContext.setLastResponse(apiClient.get(
                "/api/v1/trainer-workloads/" + username + "/years/" + year + "/months/" + month, serviceToken()));
    }

    @Then("the summary years should be in order {int}, {int}")
    public void theSummaryYearsShouldBeInOrder(int firstYear, int secondYear) {
        assertThat(years()).containsExactly(firstYear, secondYear);
    }

    @Then("the {int} summary months should be in order {int}, {int}")
    public void theYearSummaryMonthsShouldBeInOrder(int year, int firstMonth, int secondMonth) {
        JsonNode yearNode = yearNode(year);
        List<Integer> months = StreamSupport.stream(yearNode.get("months").spliterator(), false)
                .map(m -> m.get("month").asInt())
                .toList();
        assertThat(months).containsExactly(firstMonth, secondMonth);
    }

    @Then("the summary trainer status should be {word}")
    public void theSummaryTrainerStatusShouldBe(String expectedStatus) {
        JsonNode body = apiClient.json(scenarioContext.getLastResponse());
        assertThat(body.get("trainerStatus").asBoolean()).isEqualTo(Boolean.parseBoolean(expectedStatus));
    }

    @Then("the monthly training duration should be {int}")
    public void theMonthlyTrainingDurationShouldBe(int expectedDuration) {
        JsonNode body = apiClient.json(scenarioContext.getLastResponse());
        assertThat(body.get("trainingSummaryDuration").asInt()).isEqualTo(expectedDuration);
    }

    private List<Integer> years() {
        JsonNode body = apiClient.json(scenarioContext.getLastResponse());
        return StreamSupport.stream(body.get("years").spliterator(), false)
                .map(y -> y.get("year").asInt())
                .toList();
    }

    private JsonNode yearNode(int year) {
        JsonNode body = apiClient.json(scenarioContext.getLastResponse());
        for (JsonNode yearNode : body.get("years")) {
            if (yearNode.get("year").asInt() == year) {
                return yearNode;
            }
        }
        throw new AssertionError("No year " + year + " found in summary response: " + body);
    }

    private String serviceToken() {
        if (token == null) {
            token = ServiceTokenTestFactory.validServiceToken("gym-crm-service");
        }
        return token;
    }
}
