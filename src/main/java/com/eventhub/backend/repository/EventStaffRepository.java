package com.eventhub.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eventhub.backend.entity.EventStaff;

import java.util.List;
import java.util.Optional;

public interface EventStaffRepository extends JpaRepository<EventStaff, Integer> {
    List<EventStaff> findByEventIdOrderByCreatedAtDesc(Integer eventId);

    Optional<EventStaff> findByIdAndEventId(Integer id, Integer eventId);

    Optional<EventStaff> findByEventIdAndStaffId(Integer eventId, Integer staffId);

    boolean existsByEventIdAndStaffId(Integer eventId, Integer staffId);

    long countByStaffId(Integer staffId);
}
