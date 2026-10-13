package com.eventhub.backend.controller;

import com.eventhub.backend.dto.request.CreateBookingRequest;
import com.eventhub.backend.dto.response.BookingResponses.*;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookings;

    @GetMapping("/events/{eventId}/ticket-types")
    public List<TicketTypeInfo> ticketTypes(@AuthenticationPrincipal UserResponse user, @PathVariable Integer eventId) {
        return bookings.listTicketTypes(user.id(), eventId);
    }

    @PostMapping
    public ResponseEntity<Detail> create(@AuthenticationPrincipal UserResponse user,
                                         @Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookings.create(user.id(), request));
    }

    @GetMapping
    public PageResponse history(@AuthenticationPrincipal UserResponse user,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "20") int size) {
        return bookings.history(user.id(), page, size);
    }

    @GetMapping("/{bookingId}")
    public Detail detail(@AuthenticationPrincipal UserResponse user, @PathVariable Integer bookingId) {
        return bookings.get(user.id(), bookingId);
    }

    @PostMapping("/{bookingId}/payments/vnpay")
    public PaymentUrl pay(@AuthenticationPrincipal UserResponse user, @PathVariable Integer bookingId,
                          HttpServletRequest request) {
        return bookings.startPayment(user.id(), bookingId, request.getRemoteAddr());
    }
}
