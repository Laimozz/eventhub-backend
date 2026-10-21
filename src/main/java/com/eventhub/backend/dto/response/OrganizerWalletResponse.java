package com.eventhub.backend.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record OrganizerWalletResponse(
        BigDecimal pendingBalance,
        String formattedPendingBalance,
        BigDecimal availableBalance,
        String formattedAvailableBalance,
        BigDecimal withdrawalPendingBalance,
        String formattedWithdrawalPendingBalance,
        BigDecimal totalWithdrawn,
        String formattedTotalWithdrawn,
        List<EventRevenueResponse> eventRevenues
) {
    public record EventRevenueResponse(
            Integer eventId,
            String eventName,
            BigDecimal revenue,
            String formattedRevenue,
            Integer soldTickets,
            Integer totalTickets
    ) {}
}
