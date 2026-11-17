package com.eventhub.backend.service;

import com.eventhub.backend.config.ApplicationProperties;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;

@Component
public class OpenRouterClient {
    private final ApplicationProperties.Ai settings;
    private final RestClient client;

    public record ChatMessage(String role, String content) {}

    @Autowired
    public OpenRouterClient(ApplicationProperties properties) {
        this(properties.getAi(), buildClient(properties.getAi(), RestClient.builder()));
    }

    OpenRouterClient(ApplicationProperties.Ai settings, RestClient client) {
        this.settings = settings;
        this.client = client;
    }

    private static RestClient buildClient(ApplicationProperties.Ai settings, RestClient.Builder builder) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(java.time.Duration.ofSeconds(5));
        factory.setReadTimeout(settings.getTimeout());
        return builder.requestFactory(factory).baseUrl("https://openrouter.ai/api/v1").build();
    }

    public boolean available() { return settings.isAvailable(); }

    public String complete(List<ChatMessage> messages) {
        if (!available()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Trợ lý AI chưa được cấu hình. Vui lòng liên hệ quản trị viên.");
        try {
            var requestBody = new LinkedHashMap<String, Object>(Map.of(
                    "model", settings.getModel().trim(), "messages", messages,
                    "max_tokens", settings.getMaxTokens(), "stream", false));
            if (settings.getTemperature() != null) {
                requestBody.put("temperature", settings.getTemperature());
            }
            if (settings.getReasoningEnabled() != null) {
                requestBody.put("reasoning", Map.of("enabled", settings.getReasoningEnabled()));
            }
            JsonNode body = client.post().uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        headers.setBearerAuth(settings.getApiKey().trim());
                        headers.set("X-OpenRouter-Title", "EventHub Assistant");
                    })
                    .body(requestBody)
                    .retrieve().body(JsonNode.class);
            var choice = body == null ? null : body.path("choices").path(0);
            var content = choice == null ? null : choice.path("message").path("content");
            if (body == null || body.has("error") || content == null || !content.isTextual()
                    || content.asString().isBlank() || "error".equals(choice.path("finish_reason").asString())) {
                throw unavailable();
            }
            String answer = content.asString().trim();
            if (answer.length() > 16000) answer = answer.substring(0, 16000) + "\n\nBạn có thể hỏi tiếp để xem chi tiết.";
            if ("length".equals(choice.path("finish_reason").asString())) {
                answer += "\n\nCâu trả lời đã đạt giới hạn độ dài. Bạn có thể hỏi tiếp để xem phần còn lại.";
            }
            return answer;
        } catch (RestClientResponseException exception) {
            // Never forward upstream response bodies, credentials, or an upstream 401 to the browser.
            if (exception.getStatusCode().value() == 429) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Dịch vụ AI đang bận. Vui lòng thử lại sau một phút.");
            throw unavailable();
        } catch (ResourceAccessException exception) {
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT,
                    "AI phản hồi quá lâu hoặc mất kết nối. Vui lòng thử lại.");
        } catch (RestClientException exception) {
            throw unavailable();
        }
    }

    private static ResponseStatusException unavailable() {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Chưa thể kết nối trợ lý AI. Vui lòng thử lại sau hoặc liên hệ quản trị viên.");
    }
}
