package com.eventhub.backend.controller;

import com.eventhub.backend.dto.request.AdminRequests.*;
import com.eventhub.backend.dto.response.AdminResponses.*;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.service.AdminEventReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/events")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminEventReviewController {
    private final AdminEventReviewService events;
    @GetMapping("/pending")
    public PageResponse<PendingEventResponse> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) { return events.list(page, pageSize); }
    @GetMapping("/pending/{eventId}")
    public ReviewDetailResponse detail(@PathVariable Integer eventId) { return events.detail(eventId); }
    @PostMapping("/{eventId}/approve")
    public ReviewResponse approve(@AuthenticationPrincipal UserResponse admin, @PathVariable Integer eventId,
            @Valid @RequestBody ApproveEventRequest request) {
        return events.decide(admin.id(), eventId, request.version(), null);
    }
    @PostMapping("/{eventId}/reject")
    public ReviewResponse reject(@AuthenticationPrincipal UserResponse admin, @PathVariable Integer eventId,
            @Valid @RequestBody RejectEventRequest request) {
        return events.decide(admin.id(), eventId, request.version(), request.reason());
    }
}
