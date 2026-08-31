package io.github.khram0v.trainerworkload.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.service")
public record ServiceJwtProperties(
        String secret
) {
}
