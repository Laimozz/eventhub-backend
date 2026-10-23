package com.eventhub.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AdminFinanceRequests {
    private AdminFinanceRequests() {}
    public record ApproveWithdrawalRequest(@NotBlank @Size(max = 255) String transactionCode) {
        public ApproveWithdrawalRequest { if (transactionCode != null) transactionCode = transactionCode.trim(); }
    }
    public record RejectWithdrawalRequest(@NotBlank @Size(max = 255) String reason) {
        public RejectWithdrawalRequest { if (reason != null) reason = reason.trim(); }
    }
}
