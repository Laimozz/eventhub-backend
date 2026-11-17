package com.eventhub.backend.controller;

import com.eventhub.backend.dto.request.AiChatRequest;
import com.eventhub.backend.dto.response.AiChatResponses.*;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.service.AiChatService;
import com.eventhub.backend.service.AiConversationService;
import com.eventhub.backend.service.OpenRouterClient;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@PreAuthorize("hasAnyRole('CUSTOMER', 'ORGANIZER', 'STAFF', 'ADMIN')")
@RequiredArgsConstructor
public class AiChatController {
    private final AiChatService chat;
    private final AiConversationService conversations;
    private final OpenRouterClient provider;

    @GetMapping("/status")
    public Status status() { return new Status(provider.available()); }

    @GetMapping("/conversations")
    public List<Conversation> list(@AuthenticationPrincipal UserResponse user) { return conversations.list(user); }

    @GetMapping("/conversations/{id}")
    public Detail detail(@AuthenticationPrincipal UserResponse user, @PathVariable Integer id) {
        return conversations.detail(user, id);
    }

    @PostMapping("/chat")
    public Reply send(@AuthenticationPrincipal UserResponse user, @Valid @RequestBody AiChatRequest request) {
        return chat.chat(user, request);
    }

    @DeleteMapping("/conversations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserResponse user, @PathVariable Integer id) { conversations.delete(user, id); }
}
