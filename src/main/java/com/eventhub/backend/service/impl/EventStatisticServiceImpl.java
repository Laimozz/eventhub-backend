package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.response.EventStatisticResponse;
import com.eventhub.backend.dto.response.EventStatisticResponse.TicketTypeStatisticResponse;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.TicketType;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.exception.EventStatisticException;
import com.eventhub.backend.repository.EventRepository;
import com.eventhub.backend.repository.TicketTypeRepository;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.service.EventStatisticService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventStatisticServiceImpl implements EventStatisticService {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Override
    public EventStatisticResponse getEventStatistics(Integer organizerId, Integer eventId) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new EventStatisticException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                        "Chỉ Organizer đang hoạt động mới có quyền xem thống kê"));
        if (!organizer.isActive() || organizer.getRole() != Role.ORGANIZER) {
            throw new EventStatisticException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                    "Chỉ Organizer đang hoạt động mới có quyền xem thống kê");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventStatisticException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND",
                        "Không tìm thấy sự kiện"));

        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new EventStatisticException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                    "Bạn không có quyền xem thống kê sự kiện này");
        }

        // Rule 1.1: Sự kiện chưa được duyệt
        if (event.getStatus() != EventStatus.APPROVED && event.getStatus() != EventStatus.ONGOING
                && event.getStatus() != EventStatus.COMPLETED) {
            throw new EventStatisticException(HttpStatus.BAD_REQUEST, "EVENT_NOT_APPROVED",
                    "Sự kiện chưa được duyệt hoặc chưa mở bán");
        }

        List<TicketType> ticketTypes = ticketTypeRepository.findByEventIdOrderByIdAsc(eventId);

        // Rule 1.1: Sự kiện chưa mở bán (chưa tới saleStartTime của bất kỳ loại vé nào)
        LocalDateTime now = LocalDateTime.now(clock);
        boolean hasOpenedSale = !ticketTypes.isEmpty()
                && ticketTypes.stream().anyMatch(tt -> !now.isBefore(tt.getSaleStartTime()));
        if (!hasOpenedSale) {
            throw new EventStatisticException(HttpStatus.BAD_REQUEST, "EVENT_NOT_OPEN_FOR_SALE",
                    "Sự kiện chưa được duyệt hoặc chưa mở bán");
        }

        // Tính toán số liệu thống kê chi tiết theo từng loại vé
        int totalTickets = 0;
        int totalSoldTickets = 0;
        int totalRemainingTickets = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        List<TicketTypeStatisticResponse> ticketStats = new ArrayList<>();

        for (TicketType tt : ticketTypes) {
            int quantity = tt.getQuantity() != null ? tt.getQuantity() : 0;
            int remaining = tt.getRemainingQuantity() != null ? tt.getRemainingQuantity() : quantity;
            int reserved = tt.getReservedQuantity() != null ? tt.getReservedQuantity() : 0;
            int sold = Math.max(0, quantity - remaining - reserved);

            BigDecimal price = tt.getPrice() != null ? tt.getPrice() : BigDecimal.ZERO;
            BigDecimal revenue = price.multiply(BigDecimal.valueOf(sold)).setScale(2, RoundingMode.HALF_UP);

            double salesRate = quantity > 0
                    ? BigDecimal.valueOf((double) sold / quantity * 100).setScale(2, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;
            String formattedSalesRate = formatPercent(salesRate);

            totalTickets += quantity;
            totalSoldTickets += sold;
            totalRemainingTickets += remaining;
            totalRevenue = totalRevenue.add(revenue);

            ticketStats.add(new TicketTypeStatisticResponse(
                    tt.getId(),
                    tt.getName(),
                    price,
                    quantity,
                    sold,
                    remaining,
                    salesRate,
                    formattedSalesRate,
                    revenue
            ));
        }

        double overallSalesRate = totalTickets > 0
                ? BigDecimal.valueOf((double) totalSoldTickets / totalTickets * 100).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;
        String formattedOverallSalesRate = formatPercent(overallSalesRate);
        String soldRatioFormatted = totalSoldTickets + " / " + totalTickets;

        return new EventStatisticResponse(
                event.getId(),
                event.getName(),
                totalSoldTickets,
                totalTickets,
                totalRemainingTickets,
                soldRatioFormatted,
                totalRevenue,
                overallSalesRate,
                formattedOverallSalesRate,
                ticketStats
        );
    }

    private static String formatPercent(double rate) {
        if (rate == (long) rate) {
            return String.format(Locale.ROOT, "%d%%", (long) rate);
        }
        return String.format(Locale.ROOT, "%.2f%%", rate);
    }
}
