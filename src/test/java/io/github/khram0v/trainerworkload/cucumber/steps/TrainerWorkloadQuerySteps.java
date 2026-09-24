package io.github.khram0v.trainerworkload.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.github.khram0v.trainerworkload.testsupport.ApiClient;
import io.github.khram0v.trainerworkload.testsupport.ScenarioContext;
import io.github.khram0v.trainerworkload.testsupport.ServiceTokenTestFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TrainerWorkloadQuerySteps {

    private final ApiClient apiClient;
    private final ScenarioContext scenarioContext;

    private String token;

    @Given("a valid service token")
    public void aValidServiceToken() {
        token = ServiceTokenTestFactory.validServiceToken("gym-crm-service");
    }

    @Given("a non-service token")
    public void aNonServiceToken() {
        token = ServiceTokenTestFactory.nonServiceToken("gym-crm-service");
    }

    @Given("no service token")
    public void noServiceToken() {
        token = null;
    }

    @When("I request the workload summary for trainer {string}")
    public void requestWorkloadSummary(String username) {
        scenarioContext.setLastResponse(apiClient.get("/api/v1/trainer-workloads/" + username, token));
    }
}
