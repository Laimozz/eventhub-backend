package com.eventhub.backend.service;

import com.eventhub.backend.dto.request.StaffCheckInRequest;
import com.eventhub.backend.dto.response.StaffAssignedEventResponse;
import com.eventhub.backend.dto.response.StaffDtos;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.EventStaff;
import com.eventhub.backend.entity.Ticket;
import com.eventhub.backend.entity.Venue;
import com.eventhub.backend.repository.EventStaffRepository;
import com.eventhub.backend.repository.StaffTicketRepository;
import com.eventhub.backend.repository.projection.StaffProjections;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class StaffEventService {

    private final EventStaffRepository eventStaffRepository;
    private final StaffTicketRepository staffTicketRepository;

    @Transactional(readOnly = true)
    public List<StaffAssignedEventResponse> findEventsByStaff(Integer staffId) {
        List<EventStaff> assignments = eventStaffRepository.findAssignedEventsByStaffIdNative(staffId);
        return assignments.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<StaffAssignedEventResponse> findEventsByDate(Integer staffId, LocalDate date) {
        List<EventStaff> assignments = eventStaffRepository.findAssignedEventsByStaffIdAndDateNative(staffId, date);
        return assignments.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<StaffAssignedEventResponse> findEventsByStatus(Integer staffId, String status) {
        List<EventStaff> assignments = eventStaffRepository.findAssignedEventsByStaffIdAndStatusNative(staffId, status);
        return assignments.stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<StaffAssignedEventResponse> findEvents(Integer staffId, LocalDate date, String status) {
        if (date != null) {
            return findEventsByDate(staffId, date);
        }
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            return findEventsByStatus(staffId, status);
        }
        return findEventsByStaff(staffId);
    }

    @Transactional(readOnly = true)
    public List<StaffAssignedEventResponse> getMyAssignedEvents(Integer staffId) {
        return findEventsByStaff(staffId);
    }

    // Chi tiết sự kiện của Staff
    @Transactional(readOnly = true)
    public StaffDtos.StaffEventDetailDto findEventDetail(Integer staffId, Integer eventId) {
        StaffProjections.EventDetail detail = staffTicketRepository
                .findEventDetailByStaffAndEventIdNative(eventId, staffId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy sự kiện hoặc bạn chưa được phân công làm việc tại sự kiện này"));

        return new StaffDtos.StaffEventDetailDto(
                detail.getEventId(),
                detail.getEventName(),
                detail.getDescription(),
                detail.getThumbnailImageUrl(),
                detail.getBannerImageUrl(),
                detail.getImageZoneUrl(),
                detail.getStartTime(),
                detail.getEndTime(),
                detail.getEventStatus(),
                detail.getVenueCity(),
                detail.getVenueAddress(),
                detail.getVenueCapacity(),
                detail.getOrganizerName(),
                detail.getStaffStatus(),
                detail.getAssignedAt()
        );
    }

    // Lấy danh sách khách hàng đã đặt vé để gán vào select box
    @Transactional(readOnly = true)
    public List<StaffDtos.StaffCustomerDto> findCustomersByEvent(Integer staffId, Integer eventId) {
        verifyStaffAssigned(staffId, eventId);
        List<StaffProjections.CustomerSelect> customers = staffTicketRepository.findCustomersByEventIdNative(eventId);
        return customers.stream()
                .map(c -> new StaffDtos.StaffCustomerDto(c.getId(), c.getFullName(), c.getEmail(), c.getPhone(), c.getTicketCount()))
                .toList();
    }

    // Lấy danh sách loại vé (ticket types) của sự kiện để gán vào select box
    @Transactional(readOnly = true)
    public List<StaffDtos.StaffTicketTypeDto> findTicketTypesByEvent(Integer staffId, Integer eventId) {
        verifyStaffAssigned(staffId, eventId);
        List<StaffProjections.TicketTypeSelect> types = staffTicketRepository.findTicketTypesByEventIdNative(eventId);
        return types.stream()
                .map(t -> new StaffDtos.StaffTicketTypeDto(t.getId(), t.getName(), t.getPrice(), t.getQuantity()))
                .toList();
    }

    // Lấy thống kê vé (tổng vé, đã check-in thành công, chưa check-in)
    @Transactional(readOnly = true)
    public StaffDtos.StaffTicketStatsDto findTicketStatsByEvent(Integer staffId, Integer eventId) {
        verifyStaffAssigned(staffId, eventId);
        StaffProjections.TicketStats stats = staffTicketRepository.findTicketStatsByEventIdNative(eventId);
        long total = stats != null && stats.getTotalTickets() != null ? stats.getTotalTickets() : 0L;
        long checkedIn = stats != null && stats.getCheckedInTickets() != null ? stats.getCheckedInTickets() : 0L;
        long remaining = stats != null && stats.getRemainingTickets() != null ? stats.getRemainingTickets() : 0L;
        double rate = total > 0 ? Math.round(((double) checkedIn / total) * 1000.0) / 10.0 : 0.0;

        return new StaffDtos.StaffTicketStatsDto(total, checkedIn, remaining, rate);
    }

    // Lấy danh sách vé phân trang theo bộ lọc (Customer, Ticket Type, Status)
    @Transactional(readOnly = true)
    public StaffDtos.StaffTicketPageResponse findTickets(
            Integer staffId,
            Integer eventId,
            Integer customerId,
            Integer ticketTypeId,
            String status,
            int page,
            int size
    ) {
        verifyStaffAssigned(staffId, eventId);
        int pageNumber = Math.max(0, page);
        int pageSize = Math.max(1, Math.min(size, 200));
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        String normalizedStatus = (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status))
                ? null
                : status.trim().toUpperCase();

        Page<StaffProjections.TicketItem> ticketPage = staffTicketRepository
                .findTicketsByFilterNative(eventId, customerId, ticketTypeId, normalizedStatus, pageable);

        List<StaffDtos.StaffTicketDto> items = ticketPage.getContent().stream()
                .map(t -> new StaffDtos.StaffTicketDto(
                        t.getTicketId(),
                        t.getTicketCode(),
                        t.getStatus(),
                        t.getCheckedInAt(),
                        t.getCustomerId(),
                        t.getCustomerName(),
                        t.getCustomerEmail(),
                        t.getCustomerPhone(),
                        t.getTicketTypeId(),
                        t.getTicketTypeName(),
                        t.getTicketPrice(),
                        t.getCheckedInByName()
                ))
                .toList();

        return new StaffDtos.StaffTicketPageResponse(
                items,
                ticketPage.getNumber(),
                ticketPage.getSize(),
                ticketPage.getTotalElements(),
                ticketPage.getTotalPages()
        );
    }

    // Thực hiện soát vé (Check-in)
    @Transactional
    public StaffDtos.StaffCheckInResultDto checkInTicket(Integer staffId, Integer eventId, StaffCheckInRequest request) {
        verifyStaffAssigned(staffId, eventId);

        Ticket ticket = null;
        if (request.ticketId() != null) {
            ticket = staffTicketRepository.findTicketByIdAndEventIdNative(request.ticketId(), eventId)
                    .orElse(null);
        }
        if (ticket == null && request.ticketCode() != null && !request.ticketCode().isBlank()) {
            ticket = staffTicketRepository.findTicketByCodeAndEventIdNative(request.ticketCode().trim(), eventId)
                    .orElse(null);
        }

        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Không tìm thấy vé hợp lệ hoặc vé không thuộc sự kiện này");
        }

        if ("USED".equalsIgnoreCase(ticket.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Vé đã được soát vào cổng lúc " + ticket.getCheckedInAt());
        }

        if ("CANCELLED".equalsIgnoreCase(ticket.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Vé này đã bị hủy, không thể soát vé");
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        int updatedRows = staffTicketRepository.updateCheckInTicketNative(ticket.getId(), staffId, now);
        if (updatedRows == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Không thể cập nhật trạng thái vé, vui lòng thử lại");
        }

        String attendeeName = ticket.getBookingItem() != null && ticket.getBookingItem().getBooking() != null
                && ticket.getBookingItem().getBooking().getUser() != null
                ? ticket.getBookingItem().getBooking().getUser().getFullName()
                : "Khán giả";

        String ticketTypeName = ticket.getBookingItem() != null && ticket.getBookingItem().getTicketType() != null
                ? ticket.getBookingItem().getTicketType().getName()
                : "Vé vào cửa";

        return new StaffDtos.StaffCheckInResultDto(
                true,
                "Soát vé thành công",
                ticket.getId(),
                ticket.getTicketCode(),
                attendeeName,
                ticketTypeName,
                now
        );
    }

    private void verifyStaffAssigned(Integer staffId, Integer eventId) {
        if (staffTicketRepository.findEventDetailByStaffAndEventIdNative(eventId, staffId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Bạn không có quyền truy cập hoặc chưa được phân công làm việc tại sự kiện này");
        }
    }

    private StaffAssignedEventResponse mapToResponse(EventStaff es) {
        Event event = es.getEvent();
        Venue venue = event.getVenue();
        long checkedIn = eventStaffRepository.countCheckedInTicketsByEventIdNative(event.getId());
        long totalTickets = eventStaffRepository.countTotalPaidTicketsByEventIdNative(event.getId());

        return new StaffAssignedEventResponse(
                es.getId(),
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getThumbnailImageUrl(),
                event.getBannerImageUrl(),
                event.getStartTime(),
                event.getEndTime(),
                event.getStatus() != null ? event.getStatus().name() : null,
                venue != null ? venue.getCity() : null,
                venue != null ? venue.getAddress() : null,
                es.getStatus(),
                es.getCreatedAt(),
                event.getOrganizer() != null ? event.getOrganizer().getFullName() : null,
                totalTickets,
                checkedIn
        );
    }
}
