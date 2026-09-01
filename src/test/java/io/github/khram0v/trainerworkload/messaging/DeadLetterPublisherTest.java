package io.github.khram0v.trainerworkload.messaging;

import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeadLetterPublisherTest {

    @Mock private JmsTemplate jmsTemplate;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();
    private final MessagingProperties messagingProperties
            = new MessagingProperties("trainer-workload.events", "trainer-workload.events.dlq");

    private DeadLetterPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new DeadLetterPublisher(jmsTemplate, objectMapper, messagingProperties);
    }

    @Test
    void publish_sendsToConfiguredDlq() {
        publisher.publish("{\"bad\":true}", List.of("trainerUsername: must not be blank"));

        verify(jmsTemplate).send(eq("trainer-workload.events.dlq"), any(MessageCreator.class));
    }

    @Test
    void publish_messageCreatorProducesJsonWithRawPayloadAndReasons() throws Exception {
        publisher.publish("{\"bad\":true}", List.of("trainerUsername: must not be blank"));

        ArgumentCaptor<MessageCreator> creatorCaptor = ArgumentCaptor.forClass(MessageCreator.class);
        verify(jmsTemplate).send(eq("trainer-workload.events.dlq"), creatorCaptor.capture());

        Session session = mock(Session.class);
        TextMessage textMessage = mock(TextMessage.class);
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        when(session.createTextMessage(jsonCaptor.capture())).thenReturn(textMessage);

        creatorCaptor.getValue().createMessage(session);

        InvalidWorkloadEventMessage parsed =
                objectMapper.readValue(jsonCaptor.getValue(), InvalidWorkloadEventMessage.class);

        assertThat(parsed.rawPayload()).isEqualTo("{\"bad\":true}");
        assertThat(parsed.reasons()).containsExactly("trainerUsername: must not be blank");
        assertThat(parsed.rejectedAt()).isNotNull();
    }
}
