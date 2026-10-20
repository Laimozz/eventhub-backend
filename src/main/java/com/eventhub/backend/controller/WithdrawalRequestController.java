package com.eventhub.backend.controller;

import com.eventhub.backend.dto.request.CreateWithdrawalRequest;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.dto.response.WithdrawalRequestPreviewResponse;
import com.eventhub.backend.dto.response.WithdrawalRequestResponse;
import com.eventhub.backend.service.WithdrawalRequestService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizer/withdrawals")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class WithdrawalRequestController {

        private final WithdrawalRequestService withdrawalRequestService;

        @PostMapping
        public ResponseEntity<WithdrawalRequestResponse> createWithdrawalRequest(
                        @AuthenticationPrincipal UserResponse user,
                        @Valid @RequestBody CreateWithdrawalRequest request) {
                WithdrawalRequestResponse response = withdrawalRequestService.createWithdrawalRequest(user.id(),
                                request);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @PostMapping("/preview")
        public ResponseEntity<WithdrawalRequestPreviewResponse> previewWithdrawalRequest(
                        @AuthenticationPrincipal UserResponse user,
                        @Valid @RequestBody CreateWithdrawalRequest request) {
                WithdrawalRequestPreviewResponse response = withdrawalRequestService.previewWithdrawalRequest(user.id(),
                                request);
                return ResponseEntity.ok(response);
        }

        @GetMapping
        public ResponseEntity<List<WithdrawalRequestResponse>> getWithdrawalRequests(
                        @AuthenticationPrincipal UserResponse user,
                        @RequestParam(required = false) String status) {
                List<WithdrawalRequestResponse> response = withdrawalRequestService.getWithdrawalRequests(user.id(),
                                status);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/{id}")
        public ResponseEntity<WithdrawalRequestResponse> getWithdrawalRequestDetail(
                        @AuthenticationPrincipal UserResponse user,
                        @PathVariable Integer id) {
                WithdrawalRequestResponse response = withdrawalRequestService.getWithdrawalRequestDetail(user.id(), id);
                return ResponseEntity.ok(response);
        }

        @RequestMapping(value = "/{id}/cancel", method = { RequestMethod.POST, RequestMethod.PATCH })
        public ResponseEntity<WithdrawalRequestResponse> cancelWithdrawalRequest(
                        @AuthenticationPrincipal UserResponse user,
                        @PathVariable Integer id) {
                WithdrawalRequestResponse response = withdrawalRequestService.cancelWithdrawalRequest(user.id(), id);
                return ResponseEntity.ok(response);
        }
}
