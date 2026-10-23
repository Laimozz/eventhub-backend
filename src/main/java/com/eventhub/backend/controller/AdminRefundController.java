package com.eventhub.backend.controller;

import com.eventhub.backend.dto.request.RefundBankAccountRequest;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.service.RefundProviderService;
import com.eventhub.backend.service.RefundWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminRefundController {
    private final RefundWorkflowService refunds;
    private final RefundProviderService provider;

    @GetMapping("/refunds")
    public RefundWorkflowService.RefundPage list(@RequestParam(required=false) Integer eventId,
            @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int pageSize) {
        return refunds.list(eventId,page,pageSize);
    }
    @GetMapping("/refunds/{paymentId}")
    public RefundWorkflowService.Detail detail(@PathVariable Integer paymentId) { return refunds.detail(paymentId); }
    @PostMapping("/refunds/{paymentId}/automatic")
    public RefundWorkflowService.Detail queue(@PathVariable Integer paymentId,@AuthenticationPrincipal UserResponse admin) {
        return refunds.queue(paymentId,admin.id());
    }
    @PostMapping("/refund-events/{eventId}/refunds")
    public java.util.Map<String,Integer> batch(@PathVariable Integer eventId,@AuthenticationPrincipal UserResponse admin) {
        return java.util.Map.of("queuedCount",refunds.queueEvent(eventId,admin.id()));
    }
    @PostMapping("/refunds/{paymentId}/reconcile")
    public RefundWorkflowService.Detail reconcile(@PathVariable Integer paymentId) {
        provider.execute(paymentId,true); return refunds.detail(paymentId);
    }
    @PutMapping("/refunds/{paymentId}/bank-account")
    public RefundWorkflowService.Detail bank(@PathVariable Integer paymentId,@AuthenticationPrincipal UserResponse admin,
            @Valid @RequestBody RefundBankAccountRequest request) { return refunds.saveBank(paymentId,admin.id(),request); }
    @PostMapping("/refunds/{paymentId}/manual-reservation")
    public RefundWorkflowService.Detail reserve(@PathVariable Integer paymentId,@AuthenticationPrincipal UserResponse admin) {
        return refunds.reserveManual(paymentId,admin.id());
    }
    @PostMapping(value="/refunds/{paymentId}/manual-confirmation",consumes="multipart/form-data")
    public RefundWorkflowService.Detail confirm(@PathVariable Integer paymentId,@AuthenticationPrincipal UserResponse admin,
            @RequestParam String transactionCode,@RequestPart MultipartFile evidence) throws java.io.IOException {
        if(evidence.getSize()>5*1024*1024) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE,"Chứng từ tối đa 5 MB");
        return refunds.confirmManual(paymentId,admin.id(),transactionCode,evidence.getContentType(),evidence.getBytes());
    }
    @GetMapping("/refunds/{paymentId}/evidence")
    public ResponseEntity<byte[]> evidence(@PathVariable Integer paymentId) {
        var evidence=refunds.evidence(paymentId);
        return ResponseEntity.ok().header("Cache-Control","no-store").header("X-Content-Type-Options","nosniff")
                .header("Content-Disposition","attachment; filename=refund-evidence")
                .contentType(org.springframework.http.MediaType.parseMediaType(evidence.contentType())).body(evidence.content());
    }
}
