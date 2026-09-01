package io.github.khram0v.trainerworkload.messaging;

import java.time.Instant;
import java.util.List;

public record InvalidWorkloadEventMessage(
        String rawPayload,
        List<String> reasons,
        Instant rejectedAt
) {
}
