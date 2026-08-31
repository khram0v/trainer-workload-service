package io.github.khram0v.trainerworkload.controller;

import io.github.khram0v.trainerworkload.dto.response.TrainerWorkloadSummaryResponse;
import io.github.khram0v.trainerworkload.security.ServiceAccessDeniedHandler;
import io.github.khram0v.trainerworkload.security.ServiceAuthenticationEntryPoint;
import io.github.khram0v.trainerworkload.security.ServiceAuthenticationFilter;
import io.github.khram0v.trainerworkload.security.ServiceJwtProperties;
import io.github.khram0v.trainerworkload.security.ServiceTokenValidator;
import io.github.khram0v.trainerworkload.security.SecurityResponseWriter;
import io.github.khram0v.trainerworkload.security.config.SecurityConfig;
import io.github.khram0v.trainerworkload.service.TrainerWorkloadService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TrainerWorkloadController.class)
@Import({SecurityConfig.class, ServiceAuthenticationFilter.class, ServiceTokenValidator.class,
        ServiceAuthenticationEntryPoint.class, ServiceAccessDeniedHandler.class, SecurityResponseWriter.class})
@EnableConfigurationProperties(ServiceJwtProperties.class)
@TestPropertySource(properties = "security.service.secret=integration-test-shared-secret-key-of-32-bytes!!")
class TrainerWorkloadSecurityTest {

    private static final String SECRET = "integration-test-shared-secret-key-of-32-bytes!!";

    @Autowired private MockMvc mockMvc;
    @MockitoBean private TrainerWorkloadService trainerWorkloadService;

    @Test
    void request_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/trainer-workloads/Jane.Smith"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void request_withMalformedToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/trainer-workloads/Jane.Smith")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void request_withNonServiceTypeToken_returns401() throws Exception {
        String token = buildToken("someone", "access");

        mockMvc.perform(get("/api/v1/trainer-workloads/Jane.Smith")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void request_withValidServiceToken_reachesController() throws Exception {
        when(trainerWorkloadService.getSummary("Jane.Smith")).thenReturn(
                new TrainerWorkloadSummaryResponse("Jane.Smith", "Jane", "Smith", true, List.of()));
        String token = buildToken("gym-crm-service", "service");

        mockMvc.perform(get("/api/v1/trainer-workloads/Jane.Smith")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    private String buildToken(String subject, String type) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .claim("type", type)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(key)
                .compact();
    }
}
