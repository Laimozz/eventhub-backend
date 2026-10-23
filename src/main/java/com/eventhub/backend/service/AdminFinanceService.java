package com.eventhub.backend.service;

import com.eventhub.backend.dto.response.AdminFinanceResponses.*;
import com.eventhub.backend.dto.response.AdminResponses.*;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.Refund;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.exception.AdminException;
import com.eventhub.backend.repository.*;
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
}
