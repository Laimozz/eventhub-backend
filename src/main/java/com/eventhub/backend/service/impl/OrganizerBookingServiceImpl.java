package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.response.OrganizerBookingResponses.BookingItemInfo;
import com.eventhub.backend.dto.response.OrganizerBookingResponses.BookingPaymentInfo;
import com.eventhub.backend.dto.response.OrganizerBookingResponses.BookingRefundInfo;
import com.eventhub.backend.dto.response.OrganizerBookingResponses.BookingTicketInfo;
import com.eventhub.backend.dto.response.OrganizerBookingResponses.OrganizerBookingDetail;
import com.eventhub.backend.dto.response.OrganizerBookingResponses.OrganizerBookingOverview;
import com.eventhub.backend.dto.response.OrganizerBookingResponses.OrganizerBookingPageResponse;
import com.eventhub.backend.dto.response.OrganizerBookingResponses.OrganizerBookingSummary;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.exception.EventStatisticException;
import com.eventhub.backend.repository.OrganizerBookingRepository;
import com.eventhub.backend.repository.OrganizerBookingRepository.BookingDetailBaseRow;
import com.eventhub.backend.repository.OrganizerBookingRepository.BookingOverviewRow;
import com.eventhub.backend.repository.OrganizerBookingRepository.BookingRefundRow;
import com.eventhub.backend.repository.OrganizerBookingRepository.BookingSummaryRow;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.service.OrganizerBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizerBookingServiceImpl implements OrganizerBookingService {

    private final OrganizerBookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    public OrganizerBookingPageResponse getBookings(
            Integer organizerId,
            Integer eventId,
            String status,
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    ) {
        validateOrganizerAccess(organizerId);

        int validPage = Math.max(0, page);
        int validSize = size <= 0 ? 10 : Math.min(size, 100);

        List<BookingSummaryRow> rows = bookingRepository.findBookings(
                organizerId, eventId, status, keyword, startDate, endDate, validPage, validSize);
        long totalElements = bookingRepository.countBookings(
                organizerId, eventId, status, keyword, startDate, endDate);
        int totalPages = (int) Math.ceil((double) totalElements / validSize);

        BookingOverviewRow overviewRow = bookingRepository.getBookingOverview(
                organizerId, eventId, startDate, endDate);

        List<OrganizerBookingSummary> items = rows.stream().map(r -> new OrganizerBookingSummary(
                r.id(),
                r.status(),
                mapStatusLabel(r.status()),
                r.totalAmount(),
                formatVnd(r.totalAmount()),
                r.totalTickets(),
                r.createdAt(),
                r.expiredAt(),
                r.eventId(),
                r.eventName(),
                r.customerId(),
                r.customerName(),
                r.customerEmail(),
                r.customerPhone()
        )).toList();

        OrganizerBookingOverview overview = new OrganizerBookingOverview(
                overviewRow.totalBookings(),
                overviewRow.totalRevenue(),
                formatVnd(overviewRow.totalRevenue()),
                overviewRow.paidCount(),
                overviewRow.pendingCount(),
                overviewRow.cancelledCount(),
                overviewRow.otherCount()
        );

        return new OrganizerBookingPageResponse(items, validPage, validSize, totalElements, totalPages, overview);
    }

    @Override
    public OrganizerBookingDetail getBookingDetail(Integer organizerId, Integer bookingId) {
        validateOrganizerAccess(organizerId);

        BookingDetailBaseRow base = bookingRepository.getBookingDetailBase(organizerId, bookingId);
        if (base == null) {
            throw new EventStatisticException(HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND",
                    "Không tìm thấy đơn đặt vé hoặc đơn không thuộc sự kiện của bạn");
        }

        var itemRows = bookingRepository.getBookingItems(bookingId);
        List<BookingItemInfo> items = itemRows.stream().map(r -> {
            BigDecimal subtotal = r.unitPrice().multiply(BigDecimal.valueOf(r.quantity()));
            return new BookingItemInfo(
                    r.id(),
                    r.ticketTypeId(),
                    r.ticketTypeName(),
                    r.quantity(),
                    r.unitPrice(),
                    formatVnd(r.unitPrice()),
                    subtotal,
                    formatVnd(subtotal)
            );
        }).toList();

        var ticketRows = bookingRepository.getBookingTickets(bookingId);
        List<BookingTicketInfo> tickets = ticketRows.stream().map(r -> new BookingTicketInfo(
                r.id(),
                r.ticketCode(),
                r.ticketTypeName(),
                r.status(),
                r.checkedInAt(),
                r.checkedInByName()
        )).toList();

        var paymentRows = bookingRepository.getBookingPayments(bookingId);
        List<BookingPaymentInfo> payments = paymentRows.stream().map(r -> new BookingPaymentInfo(
                r.id(),
                r.transactionId(),
                r.paymentMethod(),
                r.amount(),
                formatVnd(r.amount()),
                r.status(),
                r.paidAt(),
                r.createdAt()
        )).toList();

        BookingRefundRow refundRow = bookingRepository.getBookingRefund(bookingId);
        BookingRefundInfo refund = refundRow != null ? new BookingRefundInfo(
                refundRow.id(),
                refundRow.refundTransactionId(),
                refundRow.amount(),
                formatVnd(refundRow.amount()),
                refundRow.status(),
                refundRow.refundedAt()
        ) : null;

        return new OrganizerBookingDetail(
                base.id(),
                base.status(),
                mapStatusLabel(base.status()),
                base.totalAmount(),
                formatVnd(base.totalAmount()),
                base.createdAt(),
                base.expiredAt(),
                base.eventId(),
                base.eventName(),
                base.venueName(),
                base.eventStartTime(),
                base.eventEndTime(),
                base.customerId(),
                base.customerName(),
                base.customerEmail(),
                base.customerPhone(),
                items,
                tickets,
                payments,
                refund
        );
    }

    @Override
    public byte[] exportBookingsCsv(
            Integer organizerId,
            Integer eventId,
            String status,
            String keyword,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateOrganizerAccess(organizerId);

        List<BookingSummaryRow> rows = bookingRepository.findAllBookingsForExport(
                organizerId, eventId, status, keyword, startDate, endDate);

        StringBuilder csv = new StringBuilder();
        // UTF-8 BOM for Excel
        csv.append('\uFEFF');

        csv.append("DANH SÁCH ĐƠN ĐẶT VÉ (BOOKINGS) - EVENTHUB ORGANIZER\n");
        csv.append("Ngày xuất:,").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        csv.append("Tổng số đơn:,").append(rows.size()).append("\n\n");

        csv.append("Mã đơn,Tên khách hàng,Email,Số điện thoại,Tên sự kiện,Số lượng vé,Tổng tiền (VNĐ),Trạng thái,Thời gian tạo\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (BookingSummaryRow r : rows) {
            csv.append("#").append(r.id()).append(",");
            csv.append("\"").append(escapeCsv(r.customerName())).append("\",");
            csv.append("\"").append(escapeCsv(r.customerEmail())).append("\",");
            csv.append("\"").append(escapeCsv(r.customerPhone())).append("\",");
            csv.append("\"").append(escapeCsv(r.eventName())).append("\",");
            csv.append(r.totalTickets()).append(",");
            csv.append(r.totalAmount() != null ? r.totalAmount().longValue() : 0).append(",");
            csv.append("\"").append(mapStatusLabel(r.status())).append("\",");
            csv.append(r.createdAt() != null ? r.createdAt().format(dtf) : "").append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void validateOrganizerAccess(Integer organizerId) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new EventStatisticException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                        "Chỉ Organizer đang hoạt động mới có quyền quản lý booking"));
        if (!organizer.isActive() || organizer.getRole() != Role.ORGANIZER) {
            throw new EventStatisticException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                    "Chỉ Organizer đang hoạt động mới có quyền quản lý booking");
        }
    }

    private static String formatVnd(BigDecimal amount) {
        if (amount == null) return "0 đ";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,##0", symbols);
        return formatter.format(amount) + " đ";
    }

    private static String mapStatusLabel(String status) {
        if (status == null) return "Không xác định";
        return switch (status.toUpperCase()) {
            case "PAID" -> "Đã thanh toán";
            case "PENDING" -> "Chờ thanh toán";
            case "CANCELLED", "CANCELED" -> "Đã hủy";
            case "EXPIRED" -> "Hết hạn";
            case "PAYMENT_REVIEW" -> "Chờ đối soát";
            case "REFUNDED" -> "Đã hoàn tiền";
            default -> status;
        };
    }

    private static String escapeCsv(String text) {
        if (text == null) return "";
        return text.replace("\"", "\"\"");
    }
}
