package com.eventhub.backend.controller;

import com.eventhub.backend.dto.response.TicketResponses.*;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TicketController {
    private final TicketService tickets;

    @GetMapping("/tickets")
    @PreAuthorize("hasRole('CUSTOMER')")
    public PageResponse list(@AuthenticationPrincipal UserResponse user,
                             @RequestParam(required = false) Integer bookingId,
                             @RequestParam(required = false) String status,
                             @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return tickets.list(user.id(), bookingId, status, page, size);
    }

}
