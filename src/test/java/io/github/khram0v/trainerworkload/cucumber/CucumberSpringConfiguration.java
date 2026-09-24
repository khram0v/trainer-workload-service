package io.github.khram0v.trainerworkload.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import io.github.khram0v.trainerworkload.testsupport.ApiClientTestConfiguration;
import io.github.khram0v.trainerworkload.testsupport.MongoTestContainerConfiguration;
import io.github.khram0v.trainerworkload.testsupport.ScenarioContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({
        MongoTestContainerConfiguration.class,
        ApiClientTestConfiguration.class,
        ScenarioContextConfiguration.class
})
public class CucumberSpringConfiguration {

    private static final GenericContainer<?> ACTIVE_MQ = new GenericContainer<>(
            "apache/activemq-classic:6.1.6")
            .withExposedPorts(61616);

    static {
        ACTIVE_MQ.start();
    }

    @DynamicPropertySource
    static void activeMqProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.activemq.broker-url", () ->
                "tcp://" + ACTIVE_MQ.getHost() + ":" + ACTIVE_MQ.getMappedPort(61616));
    }
}
