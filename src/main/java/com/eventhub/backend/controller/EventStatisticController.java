package com.eventhub.backend.controller;

import com.eventhub.backend.dto.response.EventStatisticResponse;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.service.EventStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events/{eventId:\\d+}/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class EventStatisticController {

    private final EventStatisticService eventStatisticService;

    @GetMapping
    public ResponseEntity<EventStatisticResponse> getEventStatistics(
            @AuthenticationPrincipal UserResponse user,
            @PathVariable Integer eventId) {
        return ResponseEntity.ok(eventStatisticService.getEventStatistics(user.id(), eventId));
    }
}
