package com.eventhub.backend.dto.response;

import com.eventhub.backend.entity.AiConversation;
import com.eventhub.backend.entity.AiMessage;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public final class AiChatResponses {
    private AiChatResponses() {}

    public record Status(boolean available) {}
    public record Source(String label, String path) {}
    public record Conversation(Integer id, String title, LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static Conversation from(AiConversation value) {
            return new Conversation(value.getId(), value.getTitle(), value.getCreatedAt(), value.getUpdatedAt());
        }
    }
    public record Message(Integer id, String role, String content, LocalDateTime createdAt) {
        public static Message from(AiMessage value) {
            return new Message(value.getId(), value.getRole(), value.getContent(), value.getCreatedAt());
        }
    }
    public record Detail(Conversation conversation, List<Message> messages) {}
    public record Reply(Conversation conversation, Message userMessage, Message assistantMessage,
            List<Source> sources, Instant contextAt) {}
}
