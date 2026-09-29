package com.eventhub.backend.repository;

import com.eventhub.backend.entity.EventStaff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventStaffRepository extends JpaRepository<EventStaff, Integer> {
}
