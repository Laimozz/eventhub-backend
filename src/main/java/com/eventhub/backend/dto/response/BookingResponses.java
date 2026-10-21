package com.eventhub.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class BookingResponses {
    private BookingResponses() {}

    public record PageResponse(List<Summary> items, int page, int size, long totalElements, int totalPages) {}

    public record Summary(Integer id, String status, BigDecimal totalAmount, String currency,
                          LocalDateTime createdAt, LocalDateTime expiredAt, LocalDateTime paymentConfirmationDeadline,
                          EventInfo event) {}
                          
    public record Detail(Integer id, String status, BigDecimal totalAmount, String currency,
                         LocalDateTime createdAt, LocalDateTime expiredAt, LocalDateTime paymentConfirmationDeadline, EventInfo event,
                         List<Item> items, List<PaymentInfo> payments) {}

    public record EventInfo(Integer id, String name, String thumbnailImageUrl, String status,
                            LocalDateTime startTime, LocalDateTime endTime, String city, String address) {}

    public record Item(Integer id, Integer ticketTypeId, String ticketTypeName, int quantity,
                       BigDecimal unitPrice, BigDecimal subtotal, List<TicketInfo> tickets) {}

    public record TicketInfo(Integer id, String ticketCode, String qrToken, String status,
                             LocalDateTime checkedInAt) {}

    public record PaymentInfo(Integer id, String transactionId, String gatewayTransactionId,
                              String paymentMethod, BigDecimal amount, String status,
                              String responseCode, LocalDateTime createdAt, LocalDateTime paidAt) {}

    public record PaymentUrl(Integer bookingId, Integer paymentId, String transactionId,
                             String paymentUrl, LocalDateTime expiredAt) {}

    public record PaymentReturn(Integer bookingId, String bookingStatus, String paymentStatus,
                                String responseCode, String transactionStatus) {}
                                
    public record TicketTypeInfo(Integer id, String name, String description, String imageUrl,
                                 BigDecimal price, String currency, int remainingQuantity,
                                 LocalDateTime saleStartTime, LocalDateTime saleEndTime, boolean bookable) {}
}
