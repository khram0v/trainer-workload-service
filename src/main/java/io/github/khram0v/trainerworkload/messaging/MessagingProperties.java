package io.github.khram0v.trainerworkload.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "messaging")
public record MessagingProperties(
        @DefaultValue("trainer-workload.events") String trainerWorkloadEventsQueue,
        @DefaultValue("trainer-workload.events.dlq") String trainerWorkloadEventsDlq
) {
}
