package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.checkedInBy.id = :staffId AND t.bookingItem.ticketType.event.id = :eventId")
    boolean existsByCheckedInByIdAndEventId(@Param("staffId") Integer staffId, @Param("eventId") Integer eventId);
}
