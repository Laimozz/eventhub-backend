package com.eventhub.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class OrganizerBookingResponses {

    private OrganizerBookingResponses() {}

    public record OrganizerBookingSummary(
            Integer id,
            String status,
            String statusLabel,
            BigDecimal totalAmount,
            String formattedTotalAmount,
            int totalTickets,
            LocalDateTime createdAt,
            LocalDateTime expiredAt,
            Integer eventId,
            String eventName,
            Integer customerId,
            String customerName,
            String customerEmail,
            String customerPhone
    ) {}

    public record OrganizerBookingOverview(
            long totalBookings,
            BigDecimal totalRevenue,
            String formattedTotalRevenue,
            long paidCount,
            long pendingCount,
            long cancelledCount,
            long otherCount
    ) {}

    public record OrganizerBookingPageResponse(
            List<OrganizerBookingSummary> items,
            int page,
            int size,
            long totalElements,
            int totalPages,
            OrganizerBookingOverview overview
    ) {}

    public record OrganizerBookingDetail(
            Integer id,
            String status,
            String statusLabel,
            BigDecimal totalAmount,
            String formattedTotalAmount,
            LocalDateTime createdAt,
            LocalDateTime expiredAt,
            Integer eventId,
            String eventName,
            String venueName,
            LocalDateTime eventStartTime,
            LocalDateTime eventEndTime,
            Integer customerId,
            String customerName,
            String customerEmail,
            String customerPhone,
            List<BookingItemInfo> items,
            List<BookingTicketInfo> tickets,
            List<BookingPaymentInfo> payments,
            BookingRefundInfo refund
    ) {}

    public record BookingItemInfo(
            Integer id,
            Integer ticketTypeId,
            String ticketTypeName,
            int quantity,
            BigDecimal unitPrice,
            String formattedUnitPrice,
            BigDecimal subtotal,
            String formattedSubtotal
    ) {}

    public record BookingTicketInfo(
            Integer id,
            String ticketCode,
            String ticketTypeName,
            String status,
            LocalDateTime checkedInAt,
            String checkedInByName
    ) {}

    public record BookingPaymentInfo(
            Integer id,
            String transactionId,
            String paymentMethod,
            BigDecimal amount,
            String formattedAmount,
            String status,
            LocalDateTime paidAt,
            LocalDateTime createdAt
    ) {}

    public record BookingRefundInfo(
            Integer id,
            String refundTransactionId,
            BigDecimal amount,
            String formattedAmount,
            String status,
            LocalDateTime refundedAt
    ) {}
}
