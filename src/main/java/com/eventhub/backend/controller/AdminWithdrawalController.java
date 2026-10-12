package com.eventhub.backend.controller;

import com.eventhub.backend.dto.request.AdminFinanceRequests.*;
import com.eventhub.backend.dto.response.AdminFinanceResponses.WithdrawalResponse;
import com.eventhub.backend.dto.response.AdminResponses.PageResponse;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.service.AdminWithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/withdrawal-requests")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminWithdrawalController {
    private final AdminWithdrawalService withdrawals;
    @GetMapping
    public PageResponse<WithdrawalResponse> list(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int pageSize) {
        return withdrawals.list(status, page, pageSize);
    }
    @GetMapping("/{requestId}")
    public WithdrawalResponse detail(@PathVariable Integer requestId) { return withdrawals.detail(requestId); }
    @PostMapping("/{requestId}/approve")
    public WithdrawalResponse approve(@AuthenticationPrincipal UserResponse admin, @PathVariable Integer requestId,
            @Valid @RequestBody ApproveWithdrawalRequest request) {
        return withdrawals.decide(admin.id(), requestId, request.transactionCode(), null);
    }
    @PostMapping("/{requestId}/reject")
    public WithdrawalResponse reject(@AuthenticationPrincipal UserResponse admin, @PathVariable Integer requestId,
            @Valid @RequestBody RejectWithdrawalRequest request) {
        return withdrawals.decide(admin.id(), requestId, null, request.reason());
    }
}
