package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.response.OrganizerReportResponse;
import com.eventhub.backend.dto.response.OrganizerReportResponse.CategoryReportItemResponse;
import com.eventhub.backend.dto.response.OrganizerReportResponse.EventFilterOptionResponse;
import com.eventhub.backend.dto.response.OrganizerReportResponse.EventReportItemResponse;
import com.eventhub.backend.dto.response.OrganizerReportResponse.OrderStatusReportItemResponse;
import com.eventhub.backend.dto.response.OrganizerReportResponse.ReportSummaryResponse;
import com.eventhub.backend.dto.response.OrganizerReportResponse.TicketTypeReportItemResponse;
import com.eventhub.backend.dto.response.OrganizerReportResponse.TimeSeriesPointResponse;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.exception.EventStatisticException;
import com.eventhub.backend.repository.EventRepository;
import com.eventhub.backend.repository.OrganizerReportRepository;
import com.eventhub.backend.repository.OrganizerReportRepository.CategoryRow;
import com.eventhub.backend.repository.OrganizerReportRepository.EventPerformanceRow;
import com.eventhub.backend.repository.OrganizerReportRepository.OrderStatusRow;
import com.eventhub.backend.repository.OrganizerReportRepository.SummaryRow;
import com.eventhub.backend.repository.OrganizerReportRepository.TicketTypeRow;
import com.eventhub.backend.repository.OrganizerReportRepository.TimeSeriesRow;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.service.OrganizerReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class OrganizerReportServiceImpl implements OrganizerReportService {

    private final OrganizerReportRepository reportRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public OrganizerReportResponse getReports(
            Integer organizerId,
            Integer eventId,
            String timeRange,
            LocalDate startDate,
            LocalDate endDate
    ) {
        validateOrganizerAccess(organizerId, eventId);

        // 1. Summary Metrics
        SummaryRow summaryRow = reportRepository.getSummaryMetrics(organizerId, eventId, timeRange, startDate, endDate);
        BigDecimal grossRev = summaryRow.totalRevenue() != null ? summaryRow.totalRevenue() : BigDecimal.ZERO;
        BigDecimal netRev = grossRev.multiply(BigDecimal.valueOf(0.95)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fee = grossRev.multiply(BigDecimal.valueOf(0.05)).setScale(2, RoundingMode.HALF_UP);

        long totalCapacity = summaryRow.totalTicketsCapacity();
        long totalSold = summaryRow.totalTicketsSold();
        long totalCheckedIn = summaryRow.totalCheckedIn();
        long totalOrders = summaryRow.totalOrders();
        BigDecimal refundAmount = summaryRow.totalRefundAmount() != null ? summaryRow.totalRefundAmount() : BigDecimal.ZERO;

        double fillRate = totalCapacity > 0
                ? BigDecimal.valueOf((double) totalSold / totalCapacity * 100).setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0.0;
        double checkinRate = totalSold > 0
                ? BigDecimal.valueOf((double) totalCheckedIn / totalSold * 100).setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0.0;
        BigDecimal aov = totalOrders > 0
                ? grossRev.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        ReportSummaryResponse summary = new ReportSummaryResponse(
                grossRev,
                formatVnd(grossRev),
                netRev,
                formatVnd(netRev),
                fee,
                formatVnd(fee),
                totalOrders,
                totalSold,
                totalCapacity,
                fillRate,
                formatPercent(fillRate),
                totalCheckedIn,
                checkinRate,
                formatPercent(checkinRate),
                aov,
                formatVnd(aov),
                refundAmount,
                formatVnd(refundAmount)
        );

        // 2. Time Series Trend
        List<TimeSeriesRow> timeSeriesRows = reportRepository.getTimeSeriesTrend(organizerId, eventId, timeRange, startDate, endDate);
        List<TimeSeriesPointResponse> timeSeries = timeSeriesRows.stream().map(r -> new TimeSeriesPointResponse(
                r.dateKey(),
                r.dateLabel(),
                r.revenue(),
                formatVnd(r.revenue()),
                r.ordersCount(),
                r.ticketsSold()
        )).toList();

        // 3. Events Breakdown
        List<EventPerformanceRow> eventRows = reportRepository.getEventPerformanceList(organizerId, eventId);
        List<EventReportItemResponse> events = eventRows.stream().map(r -> new EventReportItemResponse(
                r.eventId(),
                r.eventName(),
                r.categoryName(),
                r.status(),
                r.startTime(),
                r.endTime(),
                r.venueName(),
                r.totalCapacity(),
                r.soldTickets(),
                r.remainingTickets(),
                r.fillRate(),
                r.revenue(),
                formatVnd(r.revenue()),
                r.checkedInTickets(),
                r.checkinRate()
        )).toList();

        // 4. Ticket Types Breakdown
        List<TicketTypeRow> ticketRows = reportRepository.getTicketTypePerformanceList(organizerId, eventId);
        BigDecimal totalTicketRevenue = ticketRows.stream()
                .map(TicketTypeRow::revenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TicketTypeReportItemResponse> ticketTypes = ticketRows.stream().map(r -> {
            double share = totalTicketRevenue.compareTo(BigDecimal.ZERO) > 0 && r.revenue() != null
                    ? r.revenue().divide(totalTicketRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0.0;
            return new TicketTypeReportItemResponse(
                    r.ticketTypeId(),
                    r.ticketName(),
                    r.eventName(),
                    r.price(),
                    formatVnd(r.price()),
                    r.totalQuantity(),
                    r.soldQuantity(),
                    r.remainingQuantity(),
                    r.salesRate(),
                    r.revenue(),
                    formatVnd(r.revenue()),
                    roundOneDecimal(share)
            );
        }).toList();

        // 5. Order Status Breakdown
        List<OrderStatusRow> orderStatusRows = reportRepository.getOrderStatusBreakdown(organizerId, eventId, timeRange, startDate, endDate);
        long allOrdersCount = orderStatusRows.stream().mapToLong(OrderStatusRow::orderCount).sum();
        List<OrderStatusReportItemResponse> orderStatuses = orderStatusRows.stream().map(r -> {
            double pct = allOrdersCount > 0
                    ? BigDecimal.valueOf((double) r.orderCount() / allOrdersCount * 100).setScale(1, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;
            return new OrderStatusReportItemResponse(
                    r.status(),
                    mapOrderStatusLabel(r.status()),
                    r.orderCount(),
                    r.totalAmount(),
                    formatVnd(r.totalAmount()),
                    pct
            );
        }).toList();

        // 6. Category Breakdown
        List<CategoryRow> categoryRows = reportRepository.getCategoryBreakdown(organizerId, eventId, timeRange, startDate, endDate);
        BigDecimal totalCatRev = categoryRows.stream()
                .map(CategoryRow::revenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryReportItemResponse> categories = categoryRows.stream().map(r -> {
            double share = totalCatRev.compareTo(BigDecimal.ZERO) > 0 && r.revenue() != null
                    ? r.revenue().divide(totalCatRev, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0.0;
            return new CategoryReportItemResponse(
                    r.categoryId(),
                    r.categoryName(),
                    r.eventCount(),
                    r.soldTickets(),
                    r.revenue(),
                    formatVnd(r.revenue()),
                    roundOneDecimal(share)
            );
        }).toList();

        return new OrganizerReportResponse(
                summary,
                timeSeries,
                events,
                ticketTypes,
                orderStatuses,
                categories
        );
    }

    @Override
    public List<EventFilterOptionResponse> getEventFilterOptions(Integer organizerId) {
        validateOrganizerAccess(organizerId, null);
        var rows = reportRepository.getOrganizerEventsForFilter(organizerId);
        return rows.stream().map(r -> new EventFilterOptionResponse(
                r.id(),
                r.name(),
                r.status(),
                r.startTime()
        )).toList();
    }

    @Override
    public byte[] exportReportCsv(
            Integer organizerId,
            Integer eventId,
            String timeRange,
            LocalDate startDate,
            LocalDate endDate
    ) {
        OrganizerReportResponse reports = getReports(organizerId, eventId, timeRange, startDate, endDate);

        StringBuilder csv = new StringBuilder();
        // UTF-8 BOM for Microsoft Excel compatibility
        csv.append('\uFEFF');

        csv.append("BÁO CÁO THỐNG KÊ HOẠT ĐỘNG SỰ KIỆN - EVENTHUB\n");
        csv.append("Ngày xuất báo cáo:,").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        csv.append("Khoảng thời gian:,").append(timeRange != null ? timeRange : "Tùy chỉnh").append("\n\n");

        // 1. Chỉ số tổng hợp
        csv.append("1. CHỈ SỐ TỔNG HỢP\n");
        csv.append("Chỉ số,Giá trị\n");
        csv.append("Tổng doanh thu,").append(reports.summary().formattedTotalRevenue()).append("\n");
        csv.append("Doanh thu thực nhận (sau 5% phí),").append(reports.summary().formattedNetRevenue()).append("\n");
        csv.append("Phí nền tảng (5%),").append(reports.summary().formattedPlatformFee()).append("\n");
        csv.append("Tổng số đơn đặt vé thành công,").append(reports.summary().totalOrders()).append("\n");
        csv.append("Tổng số vé đã bán,").append(reports.summary().totalTicketsSold()).append("\n");
        csv.append("Tổng sức chứa mở bán,").append(reports.summary().totalTicketsCapacity()).append("\n");
        csv.append("Tỷ lệ lấp đầy (bán vé),").append(reports.summary().formattedFillRate()).append("\n");
        csv.append("Tổng lượt check-in thành công,").append(reports.summary().totalCheckedIn()).append("\n");
        csv.append("Tỷ lệ check-in thực tế,").append(reports.summary().formattedCheckinRate()).append("\n");
        csv.append("Giá trị trung bình mỗi đơn,").append(reports.summary().formattedAverageOrderValue()).append("\n");
        csv.append("Tổng tiền đã hoàn,").append(reports.summary().formattedTotalRefundAmount()).append("\n\n");

        // 2. Chi tiết theo sự kiện
        csv.append("2. HIỆU QUẢ THEO TỪNG SỰ KIỆN\n");
        csv.append("Mã SK,Tên sự kiện,Thể loại,Trạng thái,Thời gian bắt đầu,Địa điểm,Sức chứa,Vé đã bán,Vé còn lại,Tỷ lệ bán,Doanh thu (VNĐ),Đã check-in,Tỷ lệ check-in\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (EventReportItemResponse item : reports.events()) {
            csv.append(item.eventId()).append(",");
            csv.append("\"").append(escapeCsv(item.eventName())).append("\",");
            csv.append("\"").append(escapeCsv(item.categoryName())).append("\",");
            csv.append(item.status()).append(",");
            csv.append(item.startTime() != null ? item.startTime().format(dtf) : "").append(",");
            csv.append("\"").append(escapeCsv(item.venueName())).append("\",");
            csv.append(item.totalCapacity()).append(",");
            csv.append(item.soldTickets()).append(",");
            csv.append(item.remainingTickets()).append(",");
            csv.append(item.fillRate()).append("%,");
            csv.append(item.revenue() != null ? item.revenue().longValue() : 0).append(",");
            csv.append(item.checkedInTickets()).append(",");
            csv.append(item.checkinRate()).append("%\n");
        }
        csv.append("\n");

        // 3. Chi tiết theo loại vé
        csv.append("3. CHI TIẾT THEO LOẠI VÉ\n");
        csv.append("Mã loại vé,Tên loại vé,Sự kiện,Đơn giá (VNĐ),Tổng phát hành,Đã bán,Còn lại,Tỷ lệ bán,Doanh thu (VNĐ),Tỷ trọng DT\n");
        for (TicketTypeReportItemResponse tt : reports.ticketTypes()) {
            csv.append(tt.ticketTypeId()).append(",");
            csv.append("\"").append(escapeCsv(tt.ticketName())).append("\",");
            csv.append("\"").append(escapeCsv(tt.eventName())).append("\",");
            csv.append(tt.price() != null ? tt.price().longValue() : 0).append(",");
            csv.append(tt.totalQuantity()).append(",");
            csv.append(tt.soldQuantity()).append(",");
            csv.append(tt.remainingQuantity()).append(",");
            csv.append(tt.salesRate()).append("%,");
            csv.append(tt.revenue() != null ? tt.revenue().longValue() : 0).append(",");
            csv.append(tt.revenueShare()).append("%\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void validateOrganizerAccess(Integer organizerId, Integer eventId) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new EventStatisticException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                        "Chỉ Organizer đang hoạt động mới có quyền truy cập báo cáo"));
        if (!organizer.isActive() || organizer.getRole() != Role.ORGANIZER) {
            throw new EventStatisticException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                    "Chỉ Organizer đang hoạt động mới có quyền truy cập báo cáo");
        }

        if (eventId != null) {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EventStatisticException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND",
                            "Không tìm thấy sự kiện"));
            if (!event.getOrganizer().getId().equals(organizerId)) {
                throw new EventStatisticException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                        "Bạn không có quyền xem thống kê của sự kiện này");
            }
        }
    }

    private static String formatVnd(BigDecimal amount) {
        if (amount == null) return "0 đ";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,##0", symbols);
        return formatter.format(amount) + " đ";
    }

    private static String formatPercent(double rate) {
        if (rate == (long) rate) {
            return String.format(Locale.ROOT, "%d%%", (long) rate);
        }
        return String.format(Locale.ROOT, "%.1f%%", rate);
    }

    private static double roundOneDecimal(double val) {
        return BigDecimal.valueOf(val).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private static String mapOrderStatusLabel(String status) {
        if (status == null) return "Khác";
        return switch (status.toUpperCase()) {
            case "PAID" -> "Đã thanh toán";
            case "PENDING" -> "Chờ thanh toán";
            case "CANCELLED", "CANCELED" -> "Đã hủy";
            case "EXPIRED" -> "Hết hạn";
            case "REFUNDED" -> "Đã hoàn tiền";
            default -> status;
        };
    }

    private static String escapeCsv(String text) {
        if (text == null) return "";
        return text.replace("\"", "\"\"");
    }
}
