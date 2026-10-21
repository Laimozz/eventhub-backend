package com.eventhub.backend.repository;

import com.eventhub.backend.entity.BookingItem;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingItemRepository extends JpaRepository<BookingItem, Integer> {
    boolean existsByTicketTypeId(Integer ticketTypeId);

    @EntityGraph(attributePaths = "ticketType")
    List<BookingItem> findByBookingIdOrderByIdAsc(Integer bookingId);
}
