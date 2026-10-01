package com.eventhub.backend.security;

import com.eventhub.backend.config.ApplicationProperties;
import com.eventhub.backend.enums.Role;
import com.nimbusds.jwt.SignedJWT;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {
    private static final Instant NOW = Instant.parse("2030-01-01T00:00:00Z");
    private static final String SECRET = Base64.getEncoder().encodeToString(new byte[32]);

    @Test
    void signsAccessTokenWithFifteenMinuteLifetimeAndSessionIdentity() throws Exception {
        JwtService service = service(NOW, SECRET);
        String token = service.createAccessToken(12, 34, Role.STAFF);
        assertThat(service.verify(token)).isEqualTo(new JwtService.AccessIdentity(12, 34));
        var claims = SignedJWT.parse(token).getJWTClaimsSet();
        assertThat(Duration.between(claims.getIssueTime().toInstant(), claims.getExpirationTime().toInstant()))
                .isEqualTo(Duration.ofMinutes(15));
        assertThat(claims.getStringClaim("role")).isEqualTo("STAFF");
        assertThat(claims.getClaim("roles")).isNull();
        assertThat(claims.getStringClaim("token_type")).isEqualTo("access");
    }

    @Test
    void rejectsTokenAtExpiryWithoutGracePeriod() {
        String token = service(NOW, SECRET).createAccessToken(1, 2, Role.CUSTOMER);
        assertThatThrownBy(() -> service(NOW.plusSeconds(900), SECRET).verify(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsWrongSigningKeyAndMalformedToken() {
        String token = service(NOW, SECRET).createAccessToken(1, 2, Role.CUSTOMER);
        String otherSecret = Base64.getEncoder().encodeToString("another-test-only-key-of-32-bytes!".getBytes());
        assertThatThrownBy(() -> service(NOW, otherSecret).verify(token)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> service(NOW, SECRET).verify("not.a.jwt")).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsWrongIssuerAudienceTypeAndMissingExpiryEvenWithValidSignature() {
        for (String invalidClaim : List.of("issuer", "audience", "type", "expiry", "subject", "session")) {
            var claims = JwtClaimsSet.builder()
                    .issuer(invalidClaim.equals("issuer") ? "other" : "eventhub")
                    .audience(List.of(invalidClaim.equals("audience") ? "other" : "eventhub-api"))
                    .subject(invalidClaim.equals("subject") ? "invalid" : "1")
                    .issuedAt(NOW)
                    .claim("sid", invalidClaim.equals("session") ? "invalid" : "2")
                    .claim("token_type", invalidClaim.equals("type") ? "refresh" : "access");
            if (!invalidClaim.equals("expiry")) {
                claims.expiresAt(NOW.plusSeconds(900));
            }
            var encoder = NimbusJwtEncoder.withSecretKey(
                    new SecretKeySpec(Base64.getDecoder().decode(SECRET), "HmacSHA256")).build();
            String token = encoder.encode(JwtEncoderParameters.from(
                    JwsHeader.with(MacAlgorithm.HS256).type("JWT").build(), claims.build())).getTokenValue();
            assertThatThrownBy(() -> service(NOW, SECRET).verify(token)).as(invalidClaim).isInstanceOf(JwtException.class);
        }
    }

    @Test
    void refusesWeakOrInvalidSecretAtStartup() {
        assertThatThrownBy(() -> service(NOW, "invalid-base64")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> service(NOW, Base64.getEncoder().encodeToString(new byte[16])))
                .isInstanceOf(IllegalStateException.class);
    }

    private JwtService service(Instant now, String secret) {
        ApplicationProperties properties = new ApplicationProperties();
        properties.getAuth().setJwtSecret(secret);
        properties.getAuth().setAccessTokenTtl(Duration.ofMinutes(15));
        return new JwtService(properties, Clock.fixed(now, ZoneOffset.UTC));
    }
}
