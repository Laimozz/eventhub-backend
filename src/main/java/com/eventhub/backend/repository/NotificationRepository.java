package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    org.springframework.data.domain.Page<Notification> findByUserId(Integer userId,
            org.springframework.data.domain.Pageable pageable);

    long countByUserIdAndReadFalse(Integer userId);

    @Modifying
    @Query("update Notification n set n.read = true where n.id = :id and n.user.id = :userId")
    int markRead(Integer id, Integer userId);

    @Modifying
    @Query("update Notification n set n.read = true where n.user.id = :userId and n.read = false")
    int markAllRead(Integer userId);
}
