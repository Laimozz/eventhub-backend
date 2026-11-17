package com.eventhub.backend.service;

import com.eventhub.backend.dto.response.AiChatResponses.*;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.entity.AiConversation;
import com.eventhub.backend.entity.AiMessage;
import com.eventhub.backend.repository.AiConversationRepository;
import com.eventhub.backend.repository.AiMessageRepository;
import com.eventhub.backend.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AiConversationService {
    private final AiConversationRepository conversations;
    private final AiMessageRepository messages;
    private final UserRepository users;

    @Transactional(readOnly = true)
    public List<Conversation> list(UserResponse user) {
        return conversations.findTop50ByUser_IdAndOwnerRoleOrderByUpdatedAtDescIdDesc(user.id(), user.role())
                .stream().map(Conversation::from).toList();
    }

    @Transactional(readOnly = true)
    public Detail detail(UserResponse user, Integer id) {
        var conversation = conversations.findByIdAndUser_IdAndOwnerRole(id, user.id(), user.role())
                .orElseThrow(AiConversationService::notFound);
        return new Detail(Conversation.from(conversation), messages.findByConversation_IdOrderByIdAsc(id)
                .stream().map(Message::from).toList());
    }

    // The provider call is outside this transaction: no DB connection or row lock is held while waiting for AI.
    @Transactional
    public Detail saveExchange(UserResponse user, Integer id, Integer expectedLastMessageId,
            String question, String answer) {
        AiConversation conversation;
        if (id == null) {
            conversation = new AiConversation();
            conversation.setUser(users.getReferenceById(user.id()));
            conversation.setOwnerRole(user.role());
            conversation.setTitle(question.substring(0, Math.min(90, question.length())));
            conversation = conversations.saveAndFlush(conversation);
        } else {
            conversation = conversations.lockOwned(id, user.id(), user.role()).orElseThrow(AiConversationService::notFound);
            var existing = messages.findByConversation_IdOrderByIdAsc(id);
            Integer lastId = existing.isEmpty() ? null : existing.getLast().getId();
            if (!java.util.Objects.equals(lastId, expectedLastMessageId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Hội thoại đã thay đổi. Vui lòng mở lại hội thoại trước khi gửi tiếp.");
            }
            if (existing.size() >= 100) throw full();
        }
        var questionMessage = saveMessage(conversation, "user", question);
        var answerMessage = saveMessage(conversation, "assistant", answer);
        conversation.touch();
        conversations.flush();
        return new Detail(Conversation.from(conversation), List.of(Message.from(questionMessage), Message.from(answerMessage)));
    }

    @Transactional
    public void delete(UserResponse user, Integer id) {
        var conversation = conversations.lockOwned(id, user.id(), user.role()).orElseThrow(AiConversationService::notFound);
        messages.deleteConversationMessages(id);
        conversations.delete(conversation);
    }

    private AiMessage saveMessage(AiConversation conversation, String role, String content) {
        var message = new AiMessage();
        message.setConversation(conversation);
        message.setRole(role);
        message.setContent(content);
        return messages.save(message);
    }

    static ResponseStatusException full() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Hội thoại đã đạt 50 lượt trao đổi. Vui lòng tạo hội thoại mới.");
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hội thoại");
    }
}
