package com.eventhub.backend.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Duration;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {
    @Valid
    @NotNull
    private Auth auth = new Auth();

    @Getter
    @Setter
    public static class Auth {
        @NotBlank
        private String jwtSecret;

        @NotNull
        private Duration accessTokenTtl;

        @NotNull
        private Duration refreshTokenTtl;

        private boolean cookieSecure = true;

        @NotBlank
        @Pattern(regexp = "Lax|Strict|None")
        private String cookieSameSite = "Lax";

        @NotEmpty
        private List<@NotBlank String> allowedOrigins = List.of("http://localhost:5173", "http://localhost:3000");

        @AssertTrue(message = "Token lifetimes must be positive whole seconds")
        public boolean isTokenLifetimeValid() {
            return validLifetime(accessTokenTtl) && validLifetime(refreshTokenTtl);
        }

        private boolean validLifetime(Duration lifetime) {
            return lifetime != null && lifetime.getSeconds() > 0 && lifetime.getNano() == 0;
        }

        @AssertTrue(message = "SameSite=None requires secure cookies")
        public boolean isSameSiteSecure() {
            return !"None".equals(cookieSameSite) || cookieSecure;
        }

        @AssertTrue(message = "CORS must list explicit trusted origins; wildcards and null origins are not allowed")
        public boolean isOriginAllowlistExplicit() {
            return allowedOrigins != null && allowedOrigins.stream()
                    .allMatch(origin -> origin != null && !origin.contains("*") && !origin.equals("null"));
        }
    }
}
