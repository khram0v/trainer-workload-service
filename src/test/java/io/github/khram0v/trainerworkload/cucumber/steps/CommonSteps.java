package io.github.khram0v.trainerworkload.cucumber.steps;

import io.cucumber.java.en.Then;
import io.github.khram0v.trainerworkload.testsupport.ScenarioContext;
import lombok.RequiredArgsConstructor;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class CommonSteps {

    private final ScenarioContext scenarioContext;

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertThat(scenarioContext.getLastResponse().statusCode()).isEqualTo(expectedStatus);
    }
}
