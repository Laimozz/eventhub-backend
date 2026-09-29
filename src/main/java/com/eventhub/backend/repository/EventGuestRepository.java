package com.eventhub.backend.repository;

import com.eventhub.backend.entity.EventGuest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventGuestRepository extends JpaRepository<EventGuest, Integer> {
}
