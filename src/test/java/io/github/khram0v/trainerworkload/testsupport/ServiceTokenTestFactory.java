package io.github.khram0v.trainerworkload.testsupport;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.NoArgsConstructor;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@NoArgsConstructor
public class ServiceTokenTestFactory {

    private static final String SECRET = "local-only-dev-shared-secret-key-not-for-prod-use-1234567890";

    public static String validServiceToken(String subject) {
        return token(subject, "service", Instant.now().plusSeconds(60));
    }

    public static String nonServiceToken(String subject) {
        return token(subject, "access", Instant.now().plusSeconds(60));
    }

    private static String token(String subject, String type, Instant expiration) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .claim("type", type)
                .issuedAt(Date.from(Instant.now().minusSeconds(5)))
                .expiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }
}
