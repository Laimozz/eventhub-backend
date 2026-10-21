package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Ticket;
import com.eventhub.backend.repository.projection.StaffProjections;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StaffTicketRepository extends JpaRepository<Ticket, Integer> {

    // 1. Chi tiết sự kiện và phân công nhân viên
    @Query(value = """
            SELECT e.id AS eventId,
                   e.name AS eventName,
                   e.description AS description,
                   e.thumbnail_image_url AS thumbnailImageUrl,
                   e.banner_image_url AS bannerImageUrl,
                   e.image_zone_url AS imageZoneUrl,
                   e.start_time AS startTime,
                   e.end_time AS endTime,
                   e.status AS eventStatus,
                   v.city AS venueCity,
                   v.address AS venueAddress,
                   v.capacity AS venueCapacity,
                   org.full_name AS organizerName,
                   es.status AS staffStatus,
                   es.created_at AS assignedAt
            FROM events e
            JOIN venues v ON e.venues_id = v.id
            JOIN users org ON e.organizer_id = org.id
            JOIN event_staffs es ON es.events_id = e.id
            WHERE e.id = :eventId AND es.staff_id = :staffId
            LIMIT 1
            """, nativeQuery = true)
    Optional<StaffProjections.EventDetail> findEventDetailByStaffAndEventIdNative(
            @Param("eventId") Integer eventId,
            @Param("staffId") Integer staffId
    );

    // 2. Danh sách khách hàng đã đặt vé của sự kiện (để gán vào select box)
    @Query(value = """
            SELECT u.id AS id,
                   u.full_name AS fullName,
                   u.email AS email,
                   u.phone AS phone,
                   COUNT(t.id) AS ticketCount
            FROM users u
            JOIN bookings b ON b.users_id = u.id
            JOIN booking_items bi ON bi.bookings_id = b.id
            JOIN tickets t ON t.booking_items_id = bi.id
            WHERE b.events_id = :eventId AND b.status = 'PAID'
            GROUP BY u.id, u.full_name, u.email, u.phone
            ORDER BY u.full_name ASC
            """, nativeQuery = true)
    List<StaffProjections.CustomerSelect> findCustomersByEventIdNative(@Param("eventId") Integer eventId);

    // 3. Danh sách các loại vé (ticket types) của sự kiện (để gán vào select box)
    @Query(value = """
            SELECT tt.id AS id,
                   tt.name AS name,
                   tt.price AS price,
                   tt.quantity AS quantity
            FROM ticket_types tt
            WHERE tt.events_id = :eventId
            ORDER BY tt.id ASC
            """, nativeQuery = true)
    List<StaffProjections.TicketTypeSelect> findTicketTypesByEventIdNative(@Param("eventId") Integer eventId);

    // 4. Thống kê vé (Tổng vé, Đã check-in thành công, Chưa check-in)
    @Query(value = """
            SELECT COUNT(t.id) AS totalTickets,
                   COUNT(CASE WHEN t.status = 'USED' THEN 1 END) AS checkedInTickets,
                   COUNT(CASE WHEN t.status = 'VALID' THEN 1 END) AS remainingTickets
            FROM tickets t
            JOIN booking_items bi ON t.booking_items_id = bi.id
            JOIN bookings b ON bi.bookings_id = b.id
            WHERE b.events_id = :eventId AND b.status = 'PAID'
            """, nativeQuery = true)
    StaffProjections.TicketStats findTicketStatsByEventIdNative(@Param("eventId") Integer eventId);

    // 5. Danh sách vé phân trang theo bộ lọc (Customer, Ticket Type, Status)
    @Query(value = """
            SELECT t.id AS ticketId,
                   t.ticket_code AS ticketCode,
                   t.status AS status,
                   t.checked_in_at AS checkedInAt,
                   u.id AS customerId,
                   u.full_name AS customerName,
                   u.email AS customerEmail,
                   u.phone AS customerPhone,
                   tt.id AS ticketTypeId,
                   tt.name AS ticketTypeName,
                   tt.price AS ticketPrice,
                   staff.full_name AS checkedInByName
            FROM tickets t
            JOIN booking_items bi ON t.booking_items_id = bi.id
            JOIN bookings b ON bi.bookings_id = b.id
            JOIN ticket_types tt ON bi.ticket_types_id = tt.id
            JOIN users u ON b.users_id = u.id
            LEFT JOIN users staff ON t.checked_in_by = staff.id
            WHERE b.events_id = :eventId
              AND b.status = 'PAID'
              AND (:customerId IS NULL OR u.id = :customerId)
              AND (:ticketTypeId IS NULL OR tt.id = :ticketTypeId)
              AND (:status IS NULL OR t.status = :status)
            ORDER BY t.id ASC
            """,
            countQuery = """
            SELECT COUNT(t.id)
            FROM tickets t
            JOIN booking_items bi ON t.booking_items_id = bi.id
            JOIN bookings b ON bi.bookings_id = b.id
            JOIN ticket_types tt ON bi.ticket_types_id = tt.id
            JOIN users u ON b.users_id = u.id
            WHERE b.events_id = :eventId
              AND b.status = 'PAID'
              AND (:customerId IS NULL OR u.id = :customerId)
              AND (:ticketTypeId IS NULL OR tt.id = :ticketTypeId)
              AND (:status IS NULL OR t.status = :status)
            """,
            nativeQuery = true)
    Page<StaffProjections.TicketItem> findTicketsByFilterNative(
            @Param("eventId") Integer eventId,
            @Param("customerId") Integer customerId,
            @Param("ticketTypeId") Integer ticketTypeId,
            @Param("status") String status,
            Pageable pageable
    );

    // 6. Tìm vé theo ID và Event ID (dùng cho check-in)
    @Query(value = """
            SELECT t.* FROM tickets t
            JOIN booking_items bi ON t.booking_items_id = bi.id
            JOIN bookings b ON bi.bookings_id = b.id
            WHERE t.id = :ticketId AND b.events_id = :eventId AND b.status = 'PAID'
            LIMIT 1
            """, nativeQuery = true)
    Optional<Ticket> findTicketByIdAndEventIdNative(
            @Param("ticketId") Integer ticketId,
            @Param("eventId") Integer eventId
    );

    // 7. Tìm vé theo Ticket Code và Event ID (dùng cho check-in mã)
    @Query(value = """
            SELECT t.* FROM tickets t
            JOIN booking_items bi ON t.booking_items_id = bi.id
            JOIN bookings b ON bi.bookings_id = b.id
            WHERE t.ticket_code = :ticketCode AND b.events_id = :eventId AND b.status = 'PAID'
            LIMIT 1
            """, nativeQuery = true)
    Optional<Ticket> findTicketByCodeAndEventIdNative(
            @Param("ticketCode") String ticketCode,
            @Param("eventId") Integer eventId
    );

    // 8. Cập nhật check-in vé
    @Modifying
    @Query(value = """
            UPDATE tickets
            SET status = 'USED', checked_in_at = :checkedInAt, checked_in_by = :staffId
            WHERE id = :ticketId AND status = 'VALID'
            """, nativeQuery = true)
    int updateCheckInTicketNative(
            @Param("ticketId") Integer ticketId,
            @Param("staffId") Integer staffId,
            @Param("checkedInAt") LocalDateTime checkedInAt
    );
}
