package com.eventhub.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eventhub.backend.entity.EventStaff;

public interface EventStaffRepository extends JpaRepository<EventStaff, Integer> {
}
