package com.eventhub.backend.repository;

import com.eventhub.backend.entity.BookingItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingItemRepository extends JpaRepository<BookingItem, Integer> {
    boolean existsByTicketTypeId(Integer ticketTypeId);
}
