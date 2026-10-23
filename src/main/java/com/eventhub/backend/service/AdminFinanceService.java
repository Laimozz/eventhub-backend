package com.eventhub.backend.service;

import com.eventhub.backend.dto.response.AdminFinanceResponses.*;
import com.eventhub.backend.dto.response.AdminResponses.*;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.Refund;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.exception.AdminException;
import com.eventhub.backend.repository.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminFinanceService {
    private final AdminFinanceRepository finance;
    private final EventRepository events;
    private final PaymentRepository payments;
    private final RefundRepository refunds;

    public OverviewResponse overview() { return finance.overview(); }

    public List<AdminMonthlyTrendResponse> monthlyStats(int months) {
        var rows = finance.getMonthlyStats(months <= 0 ? 6 : months);
        return rows.stream().map(r -> new AdminMonthlyTrendResponse(
                r.monthKey(),
                r.monthLabel(),
                r.totalTransactions(),
                r.grossRevenue(),
                r.netRevenue(),
                formatVnd(r.grossRevenue()),
                formatVnd(r.netRevenue())
        )).toList();
    }

    public List<AdminStatusBreakdownResponse> eventStatusBreakdown() {
        var rows = finance.getEventStatusBreakdown();
        return rows.stream().map(r -> new AdminStatusBreakdownResponse(
                r.status(),
                statusLabel(r.status()),
                r.totalEvents(),
                r.percentage()
        )).toList();
    }

    public List<AdminCategoryRevenueResponse> categoryRevenue() {
        var rows = finance.getCategoryStats();
        return rows.stream().map(r -> new AdminCategoryRevenueResponse(
                r.categoryId(),
                r.categoryName(),
                r.totalEvents(),
                r.totalOrders(),
                r.grossRevenue(),
                formatVnd(r.grossRevenue())
        )).toList();
    }

    public PageResponse<CanceledEventResponse> canceledEvents(int page, int pageSize) {
        return PageResponse.from(events.findByStatus(EventStatus.CANCELED, AdminPagination.page(page, pageSize))
                .map(e -> new CanceledEventResponse(e.getId(), e.getName(), OrganizerResponse.from(e.getOrganizer()), e.getCancelReason())));
    }

    @Transactional(readOnly = true, isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public RefundDetailResponse refundDetail(Integer eventId, int page, int pageSize) {
        AdminPagination.validId(eventId);
        var pageable = AdminPagination.page(page, pageSize);
        Event event = events.findById(eventId).orElseThrow(() ->
                new AdminException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND", "Không tìm thấy sự kiện"));
        if (event.getStatus() != EventStatus.CANCELED)
            throw new AdminException(HttpStatus.CONFLICT, "EVENT_NOT_CANCELED", "Sự kiện chưa được hủy chính thức");
        var result = payments.findByBookingEventId(eventId, pageable);
        Map<Integer, Refund> byPayment = result.isEmpty() ? Map.of() : refunds.findByPaymentIdIn(
                result.getContent().stream().map(p -> p.getId()).toList()).stream()
                .collect(Collectors.toMap(r -> r.getPayment().getId(), Function.identity()));
        var rows = result.map(p -> {
            Refund refund = byPayment.get(p.getId());
            return new RefundPaymentResponse(p.getId(), p.getBooking().getId(), p.getBooking().getUser().getFullName(),
                    p.getAmount(), p.getStatus(), p.getPaidAt(), refund == null ? null : refund.getId(),
                    refund == null ? null : refund.getAmount(), refund == null ? null : refund.getStatus(),
                    refund == null ? null : refund.getRetryCount(), refund == null ? null : refund.getRefundedAt());
        });
        return new RefundDetailResponse(eventId, event.getName(), event.getCancelReason(), PageResponse.from(rows), true,
                "Hoàn tiền chỉ bắt đầu sau khi Admin chọn hoàn hàng loạt hoặc xử lý từng giao dịch.");
    }

    public RefundReconciliationResponse getEventRefundReconciliation(Integer eventId) {
        var row = finance.getEventRefundReconciliation(eventId);
        if (row == null) {
            return new RefundReconciliationResponse(eventId, "Không tìm thấy dữ liệu", 0, BigDecimal.ZERO, "0 đ", 0, BigDecimal.ZERO, "0 đ", 0, 0);
        }
        return new RefundReconciliationResponse(
                row.eventId(),
                row.eventName(),
                row.totalPayments(),
                row.totalAmount(),
                formatVnd(row.totalAmount()),
                row.refundedCount(),
                row.refundedAmount(),
                formatVnd(row.refundedAmount()),
                row.pendingRefundCount(),
                row.retryNeededCount()
        );
    }


    private static String statusLabel(String status) {
        if (status == null) return "Không xác định";
        return switch (status.toUpperCase()) {
            case "APPROVED" -> "Đã duyệt / Đang mở bán";
            case "ONGOING" -> "Đang diễn ra";
            case "COMPLETED" -> "Đã kết thúc";
            case "PENDING_APPROVAL" -> "Chờ phê duyệt";
            case "PENDING_CANCELLATION" -> "Chờ duyệt hủy";
            case "CANCELED" -> "Đã hủy";
            case "REJECTED" -> "Bị từ chối";
            default -> status;
        };
    }

    private static String formatVnd(BigDecimal amount) {
        if (amount == null) return "0 đ";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,##0", symbols);
        return formatter.format(amount) + " đ";
    }
}
