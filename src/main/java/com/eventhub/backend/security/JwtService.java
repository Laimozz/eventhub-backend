package com.eventhub.backend.security;

import com.eventhub.backend.config.ApplicationProperties;
import com.eventhub.backend.enums.Role;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

@Component
public class JwtService {
    private static final String ISSUER = "eventhub";
    private static final String AUDIENCE = "eventhub-api";

    private final JwtEncoder encoder;
    private final NimbusJwtDecoder decoder;
    private final Clock clock;
    private final Duration accessTokenTtl;
    private final SecureRandom random = new SecureRandom();

    public JwtService(ApplicationProperties properties, Clock clock) {
        this.clock = clock;
        this.accessTokenTtl = properties.getAuth().getAccessTokenTtl();
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(properties.getAuth().getJwtSecret());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET must be a Base64-encoded key");
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 random bytes before Base64 encoding");
        }
        var key = new SecretKeySpec(keyBytes, "HmacSHA256");
        encoder = NimbusJwtEncoder.withSecretKey(key).build();
        decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        var timestamps = new JwtTimestampValidator(Duration.ZERO);
        timestamps.setClock(clock);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(timestamps,
                new JwtIssuerValidator(ISSUER),
                new JwtClaimValidator<List<String>>("aud", audiences -> audiences != null && audiences.contains(AUDIENCE)),
                new JwtClaimValidator<String>("token_type", "access"::equals)));
    }

    public String createAccessToken(Integer userId, Integer sessionId, Role role) {
        Instant now = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER).audience(List.of(AUDIENCE))
                .subject(userId.toString()).id(UUID.randomUUID().toString())
                .issuedAt(now).expiresAt(now.plus(accessTokenTtl))
                .claim("token_type", "access").claim("sid", sessionId.toString())
                .claim("role", role.name())
                .build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).type("JWT").build(), claims)).getTokenValue();
    }

    public AccessIdentity verify(String token) {
        Jwt jwt = decoder.decode(token);
        try {
            Integer userId = Integer.valueOf(jwt.getSubject());
            Integer sessionId = Integer.valueOf(jwt.getClaimAsString("sid"));
            if (userId <= 0 || sessionId <= 0 || jwt.getExpiresAt() == null || jwt.getIssuedAt() == null
                    || !jwt.getExpiresAt().isAfter(clock.instant()) || jwt.getIssuedAt().isAfter(clock.instant())) {
                throw new IllegalArgumentException("Invalid access token claims");
            }
            return new AccessIdentity(userId, sessionId);
        } catch (IllegalArgumentException exception) {
            throw new JwtException("Invalid access token claims", exception);
        }
    }

    public String createRefreshToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hashRefreshToken(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.US_ASCII));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record AccessIdentity(Integer userId, Integer sessionId) {
    }
}
