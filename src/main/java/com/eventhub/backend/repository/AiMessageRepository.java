package com.eventhub.backend.repository;

import com.eventhub.backend.entity.AiMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiMessageRepository extends JpaRepository<AiMessage, Integer> {
}
