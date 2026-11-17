package com.eventhub.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AiChatRequest(
        @Positive Integer conversationId,
        @NotBlank(message = "Vui lòng nhập câu hỏi")
        @Size(max = 2000, message = "Câu hỏi không được vượt quá 2.000 ký tự") String message) {
}
