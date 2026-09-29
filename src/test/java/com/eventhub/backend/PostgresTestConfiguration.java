package com.eventhub.backend;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
class PostgresTestConfiguration {

    @Bean
    @ServiceConnection
    @ConditionalOnProperty(name = "test.database.mode", havingValue = "container", matchIfMissing = true)
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:16-alpine");
    }

    // Explicit opt-in for a dedicated test database when Docker is unavailable.
    @Bean
    @ConditionalOnProperty(name = "test.database.mode", havingValue = "external")
    JdbcConnectionDetails externalPostgres(Environment environment) {
        String url = environment.getRequiredProperty("TEST_DB_URL");
        String username = environment.getRequiredProperty("TEST_DB_USERNAME");
        String password = environment.getRequiredProperty("TEST_DB_PASSWORD");
        return new JdbcConnectionDetails() {
            @Override
            public String getJdbcUrl() {
                return url;
            }

            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public String getPassword() {
                return password;
            }
        };
    }
}
