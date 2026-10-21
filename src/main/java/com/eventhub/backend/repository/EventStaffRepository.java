package com.eventhub.backend.repository;

import com.eventhub.backend.entity.EventStaff;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventStaffRepository extends JpaRepository<EventStaff, Integer> {

    List<EventStaff> findByEventIdOrderByCreatedAtDesc(Integer eventId);

    Optional<EventStaff> findByIdAndEventId(Integer id, Integer eventId);

    Optional<EventStaff> findByEventIdAndStaffId(Integer eventId, Integer staffId);

    boolean existsByEventIdAndStaffId(Integer eventId, Integer staffId);

    long countByStaffId(Integer staffId);

    // 1. Lấy tất cả sự kiện nhân viên được phân công
    @Query(value = """
            SELECT es.* FROM event_staffs es
            JOIN events e ON es.events_id = e.id
            WHERE es.staff_id = :staffId
            ORDER BY e.start_time ASC
            """, nativeQuery = true)
    List<EventStaff> findAssignedEventsByStaffIdNative(@Param("staffId") Integer staffId);

    // 2. Lọc sự kiện theo ngày cụ thể (đơn giản, dễ giải thích đồ án)
    @Query(value = """
            SELECT es.* FROM event_staffs es
            JOIN events e ON es.events_id = e.id
            WHERE es.staff_id = :staffId
              AND DATE(e.start_time) = :date
            ORDER BY e.start_time ASC
            """, nativeQuery = true)
    List<EventStaff> findAssignedEventsByStaffIdAndDateNative(
            @Param("staffId") Integer staffId,
            @Param("date") LocalDate date
    );

    // 3. Lọc sự kiện theo trạng thái (APPROVED, ONGOING, COMPLETED)
    @Query(value = """
            SELECT es.* FROM event_staffs es
            JOIN events e ON es.events_id = e.id
            WHERE es.staff_id = :staffId
              AND e.status = :status
            ORDER BY e.start_time ASC
            """, nativeQuery = true)
    List<EventStaff> findAssignedEventsByStaffIdAndStatusNative(
            @Param("staffId") Integer staffId,
            @Param("status") String status
    );

    // 4. Kiểm tra phân công cụ thể của staff tại 1 sự kiện
    @Query(value = """
            SELECT es.* FROM event_staffs es
            WHERE es.events_id = :eventId AND es.staff_id = :staffId
            LIMIT 1
            """, nativeQuery = true)
    Optional<EventStaff> findAssignmentByEventIdAndStaffIdNative(
            @Param("eventId") Integer eventId,
            @Param("staffId") Integer staffId
    );

    // 5. Đếm số vé đã check-in thực tế
    @Query(value = """
            SELECT COUNT(t.id) FROM tickets t
            JOIN booking_items bi ON t.booking_items_id = bi.id
            JOIN bookings b ON bi.bookings_id = b.id
            WHERE b.events_id = :eventId AND t.checked_in_at IS NOT NULL
            """, nativeQuery = true)
    long countCheckedInTicketsByEventIdNative(@Param("eventId") Integer eventId);

    // 6. Đếm tổng số vé đã thanh toán
    @Query(value = """
            SELECT COUNT(t.id) FROM tickets t
            JOIN booking_items bi ON t.booking_items_id = bi.id
            JOIN bookings b ON bi.bookings_id = b.id
            WHERE b.events_id = :eventId AND b.status = 'PAID'
            """, nativeQuery = true)
    long countTotalPaidTicketsByEventIdNative(@Param("eventId") Integer eventId);
}
