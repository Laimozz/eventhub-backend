package com.eventhub.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WithdrawalRequestResponse(
        Integer id,
        BigDecimal amount,
        String formattedAmount,
        String bankName,
        String accountNumber,
        String status,
        String statusDisplay,
        LocalDateTime requestedAt,
        BigDecimal availableBalanceAfter,
        String formattedAvailableBalanceAfter,
        LocalDateTime processedAt,
        String rejectReason,
        String transactionCode,
        Boolean canCancel
) {
    public WithdrawalRequestResponse(
            Integer id,
            BigDecimal amount,
            String formattedAmount,
            String bankName,
            String accountNumber,
            String status,
            LocalDateTime requestedAt,
            BigDecimal availableBalanceAfter,
            String formattedAvailableBalanceAfter
    ) {
        this(id, amount, formattedAmount, bankName, accountNumber, status,
                formatStatus(status), requestedAt, availableBalanceAfter,
                formattedAvailableBalanceAfter, null, null, null,
                isPending(status));
    }

    public WithdrawalRequestResponse(
            Integer id,
            BigDecimal amount,
            String formattedAmount,
            String bankName,
            String accountNumber,
            String status,
            String statusDisplay,
            LocalDateTime requestedAt,
            BigDecimal availableBalanceAfter,
            String formattedAvailableBalanceAfter,
            LocalDateTime processedAt,
            String rejectReason,
            String transactionCode
    ) {
        this(id, amount, formattedAmount, bankName, accountNumber, status,
                statusDisplay, requestedAt, availableBalanceAfter,
                formattedAvailableBalanceAfter, processedAt, rejectReason, transactionCode,
                isPending(status));
    }

    public static boolean isPending(String status) {
        return status != null && "PENDING".equalsIgnoreCase(status.trim());
    }

    public static String formatStatus(String status) {
        if (status == null) return "";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "Chờ duyệt";
            case "APPROVED" -> "Đã duyệt";
            case "REJECTED" -> "Bị từ chối";
            case "COMPLETED" -> "Hoàn thành";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }
}
