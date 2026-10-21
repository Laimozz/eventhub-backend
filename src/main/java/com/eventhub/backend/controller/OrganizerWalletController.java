package com.eventhub.backend.controller;

import com.eventhub.backend.dto.response.OrganizerWalletResponse;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.service.OrganizerWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/organizer/wallet", "/api/organizer/revenue"})
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class OrganizerWalletController {

    private final OrganizerWalletService organizerWalletService;

    @GetMapping
    public ResponseEntity<OrganizerWalletResponse> getWalletAndRevenue(
            @AuthenticationPrincipal UserResponse user) {
        return ResponseEntity.ok(organizerWalletService.getWalletAndRevenue(user.id()));
    }
}
