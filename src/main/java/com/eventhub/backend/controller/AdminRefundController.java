package com.eventhub.backend.controller;

import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.service.RefundWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminRefundController {
    private final RefundWorkflowService refunds;
    @GetMapping("/refunds")
    public RefundWorkflowService.RefundPage list(@RequestParam(required=false) Integer eventId,
            @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int pageSize) {
        return refunds.list(eventId,page,pageSize);
    }
    @GetMapping("/refunds/{paymentId}")
    public RefundWorkflowService.Detail detail(@PathVariable Integer paymentId) { return refunds.detail(paymentId); }
    @PostMapping("/refunds/{paymentId}/complete")
    public RefundWorkflowService.Detail queue(@PathVariable Integer paymentId,@AuthenticationPrincipal UserResponse admin) {
        return refunds.complete(paymentId,admin.id());
    }
    @PostMapping("/refund-events/{eventId}/refunds")
    public java.util.Map<String,Integer> batch(@PathVariable Integer eventId,@AuthenticationPrincipal UserResponse admin) {
        return java.util.Map.of("refundedCount",refunds.completeEvent(eventId,admin.id()));
    }
}
