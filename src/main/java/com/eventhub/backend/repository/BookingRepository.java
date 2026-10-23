package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Booking;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByEventIdAndStatus(Integer eventId, String status);
    @EntityGraph(attributePaths = {"event", "event.venue"})
    Page<Booking> findByUserId(Integer userId, Pageable pageable);

    @EntityGraph(attributePaths = {"event", "event.venue"})
    Optional<Booking> findByIdAndUserId(Integer id, Integer userId);

    @Query("select b.event.id from Booking b where b.id = :id and b.user.id = :userId")
    Optional<Integer> findOwnedEventId(Integer id, Integer userId);

    @Query("""
            select b.event.id from Booking b where b.status = 'PENDING' and b.expiredAt <= :now
            and (b.expiredAt <= :graceCutoff or not exists (
                select p.id from Payment p where p.booking = b and p.paymentMethod = 'VNPAY' and p.status = 'PENDING'))
            group by b.event.id order by b.event.id
            """)
    List<Integer> findExpiredEventIds(LocalDateTime now, LocalDateTime graceCutoff, Pageable pageable);

    @Query("""
            select b from Booking b where b.event.id = :eventId and b.status = 'PENDING' and b.expiredAt <= :now
            and (b.expiredAt <= :graceCutoff or not exists (
                select p.id from Payment p where p.booking = b and p.paymentMethod = 'VNPAY' and p.status = 'PENDING'))
            order by b.id
            """)
    List<Booking> findReleasableBookings(Integer eventId, LocalDateTime now, LocalDateTime graceCutoff, Pageable pageable);
}
