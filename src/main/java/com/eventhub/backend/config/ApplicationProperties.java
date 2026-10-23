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

    @Valid
    @NotNull
    private Booking booking = new Booking();

    @Valid
    @NotNull
    private Vnpay vnpay = new Vnpay();

    @Getter
    @Setter
    public static class Booking {
        @NotNull
        private Duration holdTtl = Duration.ofMinutes(5);

        @NotNull
        private Duration ipnGracePeriod = Duration.ofSeconds(120);

        @AssertTrue(message = "Booking hold must be between 60 and 500 seconds, in whole seconds")
        public boolean isHoldTtlValid() {
            return holdTtl != null && holdTtl.getNano() == 0
                    && holdTtl.getSeconds() >= 60 && holdTtl.getSeconds() <= 500;
        }

        @AssertTrue(message = "IPN grace period must be between 0 and 300 seconds, in whole seconds")
        public boolean isIpnGracePeriodValid() {
            return ipnGracePeriod != null && ipnGracePeriod.getNano() == 0
                    && ipnGracePeriod.getSeconds() >= 0 && ipnGracePeriod.getSeconds() <= 300;
        }
    }

    @Getter
    @Setter
    public static class Vnpay {
        // Empty credentials disable checkout without preventing unrelated features from starting.
        private String tmnCode = "";
        private String hashSecret = "";
        @NotBlank
        private String paymentUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
        @NotBlank
        private String returnUrl = "http://localhost:5173/customer/payment-result";
        private String apiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";
        private String apiIpAddress = "127.0.0.1";
    }

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
