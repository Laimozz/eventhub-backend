package com.eventhub.backend.service;

import com.eventhub.backend.config.ApplicationProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class OpenRouterClientTest {
    private MockRestServiceServer server;
    private OpenRouterClient client;
    private ApplicationProperties.Ai settings;
    private final List<OpenRouterClient.ChatMessage> prompt = List.of(new OpenRouterClient.ChatMessage("user", "Xin chào"));

    @BeforeEach
    void setup() {
        settings = new ApplicationProperties.Ai();
        settings.setApiKey("test-key");
        settings.setModel("test/model");
        var builder = RestClient.builder().baseUrl("https://openrouter.ai/api/v1");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new OpenRouterClient(settings, builder.build());
    }

    @Test
    void sendsBearerKeyAndParsesChatCompletions() {
        server.expect(requestTo("https://openrouter.ai/api/v1/chat/completions"))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(jsonPath("$.model").value("test/model"))
                .andExpect(jsonPath("$.messages[0].content").value("Xin chào"))
                .andExpect(jsonPath("$.stream").value(false))
                .andExpect(jsonPath("$.reasoning").doesNotExist())
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"role":"assistant","content":"Chào bạn!"},"finish_reason":"stop"}]}
                        """, MediaType.APPLICATION_JSON));
        assertThat(client.complete(prompt)).isEqualTo("Chào bạn!");
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"nvidia/nemotron-3-ultra-550b-a55b:free", "openrouter/free", "another/provider-model:free"})
    void modelAndGenerationSettingsComeFromConfiguration(String model) {
        settings.setModel(model);
        settings.setMaxTokens(3200);
        settings.setTemperature(0.6);
        settings.setReasoningEnabled(false);
        server.expect(requestTo("https://openrouter.ai/api/v1/chat/completions"))
                .andExpect(jsonPath("$.model").value(model))
                .andExpect(jsonPath("$.reasoning.enabled").value(false))
                .andExpect(jsonPath("$.max_tokens").value(3200))
                .andExpect(jsonPath("$.temperature").value(0.6))
                .andExpect(jsonPath("$.models").doesNotExist())
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"content":"Chào bạn!","reasoning":"Internal reasoning"},"finish_reason":"stop"}]}
                        """, MediaType.APPLICATION_JSON));
        assertThat(client.complete(prompt)).isEqualTo("Chào bạn!");
        server.verify();
    }

    @Test
    void configuredReasoningCanBeEnabledAndOptionalTemperatureOmitted() {
        settings.setModel("another/reasoning-model:free");
        settings.setReasoningEnabled(true);
        settings.setTemperature(null);
        server.expect(anything())
                .andExpect(jsonPath("$.reasoning.enabled").value(true))
                .andExpect(jsonPath("$.temperature").doesNotExist())
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"content":"Chào bạn!"},"finish_reason":"stop"}]}
                        """, MediaType.APPLICATION_JSON));
        assertThat(client.complete(prompt)).isEqualTo("Chào bạn!");
        server.verify();
    }

    @Test
    void nemotronDoesNotGetAnImplicitReasoningOverride() {
        settings.setModel("nvidia/nemotron-3-ultra-550b-a55b:free");
        server.expect(anything())
                .andExpect(jsonPath("$.reasoning").doesNotExist())
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"content":"Chào bạn!"},"finish_reason":"stop"}]}
                        """, MediaType.APPLICATION_JSON));
        assertThat(client.complete(prompt)).isEqualTo("Chào bạn!");
        server.verify();
    }

    @Test
    void missingModelDisablesChatWithoutSelectingAFallback() {
        settings.setModel("");
        assertThat(client.available()).isFalse();
        assertThatThrownBy(() -> client.complete(prompt)).isInstanceOf(ResponseStatusException.class);
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 402, 403, 500})
    void upstreamFailuresDoNotExposeProviderErrorsOrExpireUserSessions(int status) {
        server.expect(anything()).andRespond(withStatus(HttpStatus.valueOf(status))
                .body("private provider diagnostic and test-key"));
        assertThatThrownBy(() -> client.complete(prompt)).isInstanceOfSatisfying(ResponseStatusException.class, error -> {
            assertThat(error.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            assertThat(error.getReason()).doesNotContain("test-key", "diagnostic");
        });
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"error\":{\"message\":\"secret\"}}", "{\"choices\":[{\"message\":{\"content\":\"\"}}]}"})
    void rejectsEmptyOrErrorResponses(String body) {
        server.expect(anything()).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> client.complete(prompt)).isInstanceOfSatisfying(ResponseStatusException.class,
                error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    void propagatesRateLimitAsRetryableError() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        assertThatThrownBy(() -> client.complete(prompt)).isInstanceOfSatisfying(ResponseStatusException.class,
                error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
    }

    @Test
    void missingKeyDoesNotCallProvider() {
        settings.setApiKey("");
        assertThat(client.available()).isFalse();
        assertThatThrownBy(() -> client.complete(prompt)).isInstanceOf(ResponseStatusException.class);
        server.verify();
    }
}
