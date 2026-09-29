package com.eventhub.backend.repository;

import com.eventhub.backend.entity.UserEventInteraction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEventInteractionRepository extends JpaRepository<UserEventInteraction, Integer> {
}
