package io.github.khram0v.trainerworkload.cucumber.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.khram0v.trainerworkload.testsupport.ServiceTokenTestFactory;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

public class TrainerWorkloadQuerySteps {

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String token;
    private HttpResponse<String> response;

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
    public void requestWorkloadSummary(String username) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/v1/trainer-workloads/" + username))
                .GET();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int expectedStatus) {
        assertThat(response.statusCode()).isEqualTo(expectedStatus);
    }
}
