package com.eventhub.backend.config;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationPropertiesTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfiguration.class)
            .withPropertyValues("app.auth.jwt-secret=test-only",
                    "app.auth.access-token-ttl=2m", "app.auth.refresh-token-ttl=3d");

    @Test
    void bindsApplicationSettingsAndSupportsHttpLocalCookies() {
        runner.withPropertyValues("app.auth.cookie-secure=false", "app.auth.cookie-same-site=Lax",
                        "app.auth.allowed-origins=http://localhost:5173")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    var auth = context.getBean(ApplicationProperties.class).getAuth();
                    assertThat(auth.getAccessTokenTtl()).isEqualTo(Duration.ofMinutes(2));
                    assertThat(auth.getRefreshTokenTtl()).isEqualTo(Duration.ofDays(3));
                    assertThat(auth.isCookieSecure()).isFalse();
                    assertThat(auth.getAllowedOrigins()).containsExactly("http://localhost:5173");
                });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "app.auth.access-token-ttl=0s", "app.auth.access-token-ttl=-1m",
            "app.auth.refresh-token-ttl=0s", "app.auth.refresh-token-ttl=-1d",
            "app.auth.access-token-ttl=500ms", "app.auth.refresh-token-ttl=invalid"
    })
    void rejectsInvalidTokenLifetimesAtStartup(String property) {
        runner.withPropertyValues(property).run(context -> assertThat(context).hasFailed());
    }

    @Test
    void rejectsInsecureSameSiteNone() {
        runner.withPropertyValues("app.auth.cookie-secure=false", "app.auth.cookie-same-site=None")
                .run(context -> assertThat(context).hasFailed());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0s", "-1m", "59s", "31m", "90500ms"})
    void rejectsInvalidBookingHoldLifetime(String value) {
        runner.withPropertyValues("app.booking.hold-ttl=" + value)
                .run(context -> assertThat(context).hasFailed());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1s", "301s", "500ms"})
    void rejectsInvalidIpnGracePeriod(String value) {
        runner.withPropertyValues("app.booking.ipn-grace-period=" + value)
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void bindsIpnGracePeriodAndAllowsDisablingIt() {
        runner.run(context -> assertThat(context.getBean(ApplicationProperties.class).getBooking().getIpnGracePeriod())
                .isEqualTo(Duration.ofSeconds(120)));
        runner.withPropertyValues("app.booking.ipn-grace-period=0s").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(ApplicationProperties.class).getBooking().getIpnGracePeriod()).isZero();
        });
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableConfigurationProperties(ApplicationProperties.class)
    static class PropertiesConfiguration {
    }
}
