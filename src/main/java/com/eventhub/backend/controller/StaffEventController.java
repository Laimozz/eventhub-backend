package com.eventhub.backend.controller;

import com.eventhub.backend.dto.request.StaffCheckInRequest;
import com.eventhub.backend.dto.response.StaffAssignedEventResponse;
import com.eventhub.backend.dto.response.StaffDtos;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.service.StaffEventService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffEventController {

    private final StaffEventService staffEventService;

    // 1. Danh sách sự kiện staff được phân công
    @GetMapping("/events")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<StaffAssignedEventResponse>> getMyAssignedEvents(
            @AuthenticationPrincipal UserResponse currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(staffEventService.findEvents(currentUser.id(), date, status));
    }

    // 2. Chi tiết sự kiện của Staff
    @GetMapping("/events/{eventId}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<StaffDtos.StaffEventDetailDto> getStaffEventDetail(
            @AuthenticationPrincipal UserResponse currentUser,
            @PathVariable Integer eventId
    ) {
        return ResponseEntity.ok(staffEventService.findEventDetail(currentUser.id(), eventId));
    }

    // 3. Danh sách khách hàng đã đặt vé để gán vào select box
    @GetMapping("/events/{eventId}/customers")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<StaffDtos.StaffCustomerDto>> getCustomersByEvent(
            @AuthenticationPrincipal UserResponse currentUser,
            @PathVariable Integer eventId
    ) {
        return ResponseEntity.ok(staffEventService.findCustomersByEvent(currentUser.id(), eventId));
    }

    // 4. Danh sách loại vé của sự kiện để gán vào select box
    @GetMapping("/events/{eventId}/ticket-types")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<StaffDtos.StaffTicketTypeDto>> getTicketTypesByEvent(
            @AuthenticationPrincipal UserResponse currentUser,
            @PathVariable Integer eventId
    ) {
        return ResponseEntity.ok(staffEventService.findTicketTypesByEvent(currentUser.id(), eventId));
    }

    // 5. Thống kê vé (tổng vé, đã check-in thành công, chưa check-in)
    @GetMapping("/events/{eventId}/stats")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<StaffDtos.StaffTicketStatsDto> getTicketStatsByEvent(
            @AuthenticationPrincipal UserResponse currentUser,
            @PathVariable Integer eventId
    ) {
        return ResponseEntity.ok(staffEventService.findTicketStatsByEvent(currentUser.id(), eventId));
    }

    // 6. Danh sách vé phân trang theo bộ lọc (Customer, Ticket Type, Status, limit/page)
    @GetMapping("/events/{eventId}/tickets")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<StaffDtos.StaffTicketPageResponse> getTicketsByFilter(
            @AuthenticationPrincipal UserResponse currentUser,
            @PathVariable Integer eventId,
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) Integer ticketTypeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(staffEventService.findTickets(
                currentUser.id(), eventId, customerId, ticketTypeId, status, page, size
        ));
    }

    // 7. Thực hiện check-in soát vé
    @PostMapping("/events/{eventId}/check-in")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<StaffDtos.StaffCheckInResultDto> checkInTicket(
            @AuthenticationPrincipal UserResponse currentUser,
            @PathVariable Integer eventId,
            @RequestBody StaffCheckInRequest request
    ) {
        return ResponseEntity.ok(staffEventService.checkInTicket(currentUser.id(), eventId, request));
    }
}
