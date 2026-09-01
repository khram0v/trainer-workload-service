package io.github.khram0v.trainerworkload.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeadLetterEventListener {

    @JmsListener(destination = "${messaging.trainer-workload-events-dlq}")
    public void onDeadLetter(String payload) {
        log.error("Received dead-lettered workload event, manual investigation required: {}", payload);
    }
}
