package com.eventhub.backend.repository;

import com.eventhub.backend.entity.EventGuest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventGuestRepository extends JpaRepository<EventGuest, Integer> {
    List<EventGuest> findByEventIdOrderByIdAsc(Integer eventId);
}
