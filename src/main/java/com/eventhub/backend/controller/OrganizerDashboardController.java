package com.eventhub.backend.controller;

import com.eventhub.backend.dto.response.OrganizerDashboardResponses.CheckinAlertResponse;
import com.eventhub.backend.dto.response.OrganizerDashboardResponses.EventPerformanceResponse;
import com.eventhub.backend.dto.response.OrganizerDashboardResponses.MonthlyStatsResponse;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.service.OrganizerDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/organizer/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class OrganizerDashboardController {

    private final OrganizerDashboardService service;

    @GetMapping("/monthly-stats")
    public ResponseEntity<List<MonthlyStatsResponse>> getMonthlyStats(
            @AuthenticationPrincipal UserResponse user,
            @RequestParam(defaultValue = "6m") String interval) {
        return ResponseEntity.ok(service.getMonthlyStats(user.id(), interval));
    }

    @GetMapping("/event-performance")
    public ResponseEntity<List<EventPerformanceResponse>> getEventPerformance(
            @AuthenticationPrincipal UserResponse user,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(service.getEventPerformance(user.id(), limit));
    }

    @GetMapping("/checkin-alert")
    public ResponseEntity<CheckinAlertResponse> getCheckinAlert(
            @AuthenticationPrincipal UserResponse user) {
        return ResponseEntity.ok(service.getCheckinAlert(user.id()));
    }

    @GetMapping("/staff-dispatch/summary")
    public ResponseEntity<com.eventhub.backend.dto.response.OrganizerDashboardResponses.StaffDispatchSummaryResponse> getStaffDispatchSummary(
            @AuthenticationPrincipal UserResponse user) {
        return ResponseEntity.ok(service.getStaffDispatchSummary(user.id()));
    }

    @GetMapping("/staff-dispatch/allocations")
    public ResponseEntity<List<com.eventhub.backend.dto.response.OrganizerDashboardResponses.EventStaffDispatchResponse>> getEventStaffAllocations(
            @AuthenticationPrincipal UserResponse user) {
        return ResponseEntity.ok(service.getEventStaffAllocations(user.id()));
    }
}

