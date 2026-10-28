package com.eventhub.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrganizerReportResponse(
        ReportSummaryResponse summary,
        List<TimeSeriesPointResponse> timeSeries,
        List<EventReportItemResponse> events,
        List<TicketTypeReportItemResponse> ticketTypes,
        List<OrderStatusReportItemResponse> orderStatuses,
        List<CategoryReportItemResponse> categories
) {
    public record ReportSummaryResponse(
            BigDecimal totalRevenue,
            String formattedTotalRevenue,
            BigDecimal netRevenue,
            String formattedNetRevenue,
            BigDecimal platformFee,
            String formattedPlatformFee,
            long totalOrders,
            long totalTicketsSold,
            long totalTicketsCapacity,
            double fillRate,
            String formattedFillRate,
            long totalCheckedIn,
            double checkinRate,
            String formattedCheckinRate,
            BigDecimal averageOrderValue,
            String formattedAverageOrderValue,
            BigDecimal totalRefundAmount,
            String formattedTotalRefundAmount
    ) {}

    public record TimeSeriesPointResponse(
            String dateKey,
            String dateLabel,
            BigDecimal revenue,
            String formattedRevenue,
            long ordersCount,
            long ticketsSold
    ) {}

    public record EventReportItemResponse(
            Integer eventId,
            String eventName,
            String categoryName,
            String status,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String venueName,
            int totalCapacity,
            int soldTickets,
            int remainingTickets,
            double fillRate,
            BigDecimal revenue,
            String formattedRevenue,
            int checkedInTickets,
            double checkinRate
    ) {}

    public record TicketTypeReportItemResponse(
            Integer ticketTypeId,
            String ticketName,
            String eventName,
            BigDecimal price,
            String formattedPrice,
            int totalQuantity,
            int soldQuantity,
            int remainingQuantity,
            double salesRate,
            BigDecimal revenue,
            String formattedRevenue,
            double revenueShare
    ) {}

    public record OrderStatusReportItemResponse(
            String status,
            String statusLabel,
            long orderCount,
            BigDecimal totalAmount,
            String formattedTotalAmount,
            double percentage
    ) {}

    public record CategoryReportItemResponse(
            Integer categoryId,
            String categoryName,
            long eventCount,
            long soldTickets,
            BigDecimal revenue,
            String formattedRevenue,
            double revenueShare
    ) {}

    public record EventFilterOptionResponse(
            Integer id,
            String name,
            String status,
            LocalDateTime startTime
    ) {}
}
