package io.github.khram0v.trainerworkload.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class ServiceTokenValidator {

    private static final String TYPE_CLAIM = "type";
    private static final String SERVICE_TOKEN_TYPE = "service";

    private final SecretKey key;

    public ServiceTokenValidator(ServiceJwtProperties properties) {
        byte[] secretBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("Service JWT secret must be at least 32 bytes long");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public String validateAndExtractSubject(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (!SERVICE_TOKEN_TYPE.equals(claims.get(TYPE_CLAIM, String.class))) {
            throw new JwtException("Not a service token");
        }

        return claims.getSubject();
    }
}
