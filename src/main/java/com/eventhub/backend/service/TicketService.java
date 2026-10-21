package com.eventhub.backend.service;

import com.eventhub.backend.dto.response.BookingResponses.EventInfo;
import com.eventhub.backend.dto.response.TicketResponses.*;
import com.eventhub.backend.entity.Ticket;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.repository.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository tickets;
    private final BookingRepository bookings;
    private final UserRepository users;
    private final Clock clock;

    @Transactional(readOnly = true)
    public PageResponse list(Integer userId, Integer bookingId, String status, int page, int size) {
        requireCustomer(userId);
        if (page < 0 || size < 1 || size > 50 || (status != null && !List.of("VALID", "USED").contains(status))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phân trang hoặc trạng thái vé không hợp lệ");
        }
        if (bookingId != null && bookings.findOwnedEventId(bookingId, userId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy booking");
        }
        var result = tickets.findCustomerTickets(userId, bookingId, status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id")));
        return new PageResponse(result.stream().map(this::ticketResponse).toList(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    private TicketResponse ticketResponse(Ticket ticket) {
        var booking = ticket.getBookingItem().getBooking();
        var event = booking.getEvent();
        boolean usable = canCheckIn(ticket);
        return new TicketResponse(ticket.getId(), booking.getId(), ticket.getTicketCode(), usable ? ticket.getQrToken() : null,
                ticket.getStatus(), ticket.getBookingItem().getTicketType().getName(), ticket.getBookingItem().getUnitPrice(),
                booking.getUser().getFullName(), ticket.getCheckedInAt(), usable, new EventInfo(event.getId(), event.getName(),
                event.getThumbnailImageUrl(), event.getStatus().name(), event.getStartTime(), event.getEndTime(),
                event.getVenue().getCity(), event.getVenue().getAddress()));
    }

    private boolean canCheckIn(Ticket ticket) {
        var booking = ticket.getBookingItem().getBooking();
        var event = booking.getEvent();
        return "VALID".equals(ticket.getStatus()) && ticket.getCheckedInAt() == null && "PAID".equals(booking.getStatus())
                && (event.getStatus() == EventStatus.APPROVED || event.getStatus() == EventStatus.ONGOING)
                && event.getEndTime().isAfter(LocalDateTime.now(clock));
    }

    private void requireCustomer(Integer id) {
        User user = users.findById(id).orElseThrow(() -> new AccessDeniedException("Tài khoản không khả dụng"));
        if (!user.isActive() || user.getRole() != Role.CUSTOMER) throw new AccessDeniedException("Bạn không có quyền thực hiện thao tác này");
    }
}
