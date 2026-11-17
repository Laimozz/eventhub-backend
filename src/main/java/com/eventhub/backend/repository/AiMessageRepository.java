package com.eventhub.backend.repository;

import com.eventhub.backend.entity.AiMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AiMessageRepository extends JpaRepository<AiMessage, Integer> {
    List<AiMessage> findByConversation_IdOrderByIdAsc(Integer conversationId);

    @Modifying
    @Query("delete from AiMessage m where m.conversation.id = :conversationId")
    void deleteConversationMessages(Integer conversationId);
}
