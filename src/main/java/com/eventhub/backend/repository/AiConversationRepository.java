package com.eventhub.backend.repository;

import com.eventhub.backend.entity.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiConversationRepository extends JpaRepository<AiConversation, Integer> {
}
