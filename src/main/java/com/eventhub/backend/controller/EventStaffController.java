package com.eventhub.backend.controller;

import com.eventhub.backend.dto.request.AddEventStaffRequest;
import com.eventhub.backend.dto.request.UpdateEventStaffStatusRequest;
import com.eventhub.backend.dto.response.EventStaffResponse;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.service.EventStaffService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events/{eventId:\\d+}/staffs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class EventStaffController {

    private final EventStaffService eventStaffService;

    @GetMapping
    public List<EventStaffResponse> listStaffs(@AuthenticationPrincipal UserResponse user,
            @PathVariable Integer eventId) {
        return eventStaffService.getStaffs(user.id(), eventId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventStaffResponse addStaff(@AuthenticationPrincipal UserResponse user,
            @PathVariable Integer eventId,
            @Valid @RequestBody AddEventStaffRequest request) {
        return eventStaffService.addStaff(user.id(), eventId, request);
    }

    @PatchMapping("/{staffId:\\d+}/status")
    public EventStaffResponse updateStaffStatus(@AuthenticationPrincipal UserResponse user,
            @PathVariable Integer eventId,
            @PathVariable Integer staffId,
            @Valid @RequestBody UpdateEventStaffStatusRequest request) {
        return eventStaffService.updateStaffStatus(user.id(), eventId, staffId, request);
    }

    @DeleteMapping("/{staffId:\\d+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStaff(@AuthenticationPrincipal UserResponse user,
            @PathVariable Integer eventId,
            @PathVariable Integer staffId) {
        eventStaffService.deleteStaff(user.id(), eventId, staffId);
    }
}
