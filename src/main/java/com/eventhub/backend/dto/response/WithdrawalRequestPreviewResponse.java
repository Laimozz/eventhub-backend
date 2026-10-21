package com.eventhub.backend.dto.response;

import java.math.BigDecimal;

public record WithdrawalRequestPreviewResponse(
        BigDecimal amount,
        String formattedAmount,
        String bankName,
        String accountNumber,
        BigDecimal currentAvailableBalance,
        String formattedCurrentAvailableBalance,
        BigDecimal availableBalanceAfter,
        String formattedAvailableBalanceAfter
) {}
