package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    org.springframework.data.domain.Page<Notification> findByUserId(Integer userId,
            org.springframework.data.domain.Pageable pageable);
}
