package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Ticket;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    @Modifying(flushAutomatically = true)
    @Query(value = """
            UPDATE tickets SET status = 'CANCELED'
            WHERE status = 'VALID' AND booking_items_id IN (
                SELECT i.id FROM booking_items i JOIN bookings b ON b.id = i.bookings_id
                WHERE b.events_id = :eventId
            )
            """, nativeQuery = true)
    int invalidateUnusedForEvent(@Param("eventId") Integer eventId);

    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.checkedInBy.id = :staffId AND t.bookingItem.ticketType.event.id = :eventId")
    boolean existsByCheckedInByIdAndEventId(@Param("staffId") Integer staffId, @Param("eventId") Integer eventId);

    List<Ticket> findByBookingItemBookingIdOrderByIdAsc(Integer bookingId);

    @EntityGraph(attributePaths = {"bookingItem.ticketType", "bookingItem.booking.user", "bookingItem.booking.event.venue"})
    @Query("""
            select t from Ticket t where t.bookingItem.booking.user.id = :userId
            and (:bookingId is null or t.bookingItem.booking.id = :bookingId)
            and (:status is null or t.status = :status)
            """)
    Page<Ticket> findCustomerTickets(Integer userId, Integer bookingId, String status, Pageable pageable);
}
