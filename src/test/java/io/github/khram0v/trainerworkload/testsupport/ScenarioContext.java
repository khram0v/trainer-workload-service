package io.github.khram0v.trainerworkload.testsupport;

import lombok.Getter;
import lombok.Setter;

import java.net.http.HttpResponse;

@Getter
@Setter
public class ScenarioContext {

    private String trainerUsername;
    private HttpResponse<String> lastResponse;
}
