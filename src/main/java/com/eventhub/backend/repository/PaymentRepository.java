package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"booking", "booking.user"})
    org.springframework.data.domain.Page<Payment> findByBookingEventId(Integer eventId, org.springframework.data.domain.Pageable pageable);
}
