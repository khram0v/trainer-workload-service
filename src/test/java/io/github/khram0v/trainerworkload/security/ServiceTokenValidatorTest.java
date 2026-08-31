package io.github.khram0v.trainerworkload.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceTokenValidatorTest {

    private static final String SECRET = "test-service-secret-key-must-be-at-least-32-bytes!!";

    private ServiceTokenValidator validator;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        validator = new ServiceTokenValidator(new ServiceJwtProperties(SECRET));
        key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void constructor_whenSecretTooShort_throwsIllegalStateException() {
        assertThatThrownBy(() -> new ServiceTokenValidator(new ServiceJwtProperties("short")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    void validateAndExtractSubject_whenValidServiceToken_returnsSubject() {
        String token = buildToken("gym-crm-service", "service", Instant.now().plusSeconds(60));

        assertThat(validator.validateAndExtractSubject(token)).isEqualTo("gym-crm-service");
    }

    @Test
    void validateAndExtractSubject_whenTypeClaimIsNotService_throws() {
        String token = buildToken("gym-crm-service", "access", Instant.now().plusSeconds(60));

        assertThatThrownBy(() -> validator.validateAndExtractSubject(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void validateAndExtractSubject_whenTokenExpired_throws() {
        String token = buildToken("gym-crm-service", "service", Instant.now().minusSeconds(60));

        assertThatThrownBy(() -> validator.validateAndExtractSubject(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void validateAndExtractSubject_whenSignedWithDifferentSecret_throws() {
        SecretKey otherKey = Keys.hmacShaKeyFor(
                "a-completely-different-shared-secret-of-32-bytes!!".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("gym-crm-service")
                .claim("type", "service")
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(otherKey)
                .compact();

        assertThatThrownBy(() -> validator.validateAndExtractSubject(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void validateAndExtractSubject_whenTokenMalformed_throws() {
        assertThatThrownBy(() -> validator.validateAndExtractSubject("not-a-jwt"))
                .isInstanceOf(JwtException.class);
    }

    private String buildToken(String subject, String type, Instant expiration) {
        return Jwts.builder()
                .subject(subject)
                .claim("type", type)
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }
}
