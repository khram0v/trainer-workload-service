package io.github.khram0v.trainerworkload.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DeadLetterPublisher {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final MessagingProperties messagingProperties;

    public void publish(String rawPayload, List<String> reasons) {
        InvalidWorkloadEventMessage deadLetter = new InvalidWorkloadEventMessage(rawPayload, reasons, Instant.now());
        String body = objectMapper.writeValueAsString(deadLetter);

        jmsTemplate.send(messagingProperties.trainerWorkloadEventsDlq(),
                session -> session.createTextMessage(body));

        log.warn("Routed invalid workload event to dead letter queue '{}': {}",
                messagingProperties.trainerWorkloadEventsDlq(), reasons);
    }
}
