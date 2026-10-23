package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.response.OrganizerWalletResponse;
import com.eventhub.backend.dto.response.OrganizerWalletResponse.EventRevenueResponse;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.OrganizerWallet;
import com.eventhub.backend.entity.TicketType;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.exception.OrganizerWalletException;
import com.eventhub.backend.repository.EventRepository;
import com.eventhub.backend.repository.OrganizerWalletRepository;
import com.eventhub.backend.repository.TicketTypeRepository;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.service.OrganizerWalletService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizerWalletServiceImpl implements OrganizerWalletService {

    private final OrganizerWalletRepository organizerWalletRepository;
    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final UserRepository userRepository;
    private final com.eventhub.backend.repository.FinanceLedgerRepository financeLedger;

    @Override
    public OrganizerWalletResponse getWalletAndRevenue(Integer organizerId) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new OrganizerWalletException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                        "Chỉ Organizer đang hoạt động mới có quyền truy cập"));
        if (!organizer.isActive() || organizer.getRole() != Role.ORGANIZER) {
            throw new OrganizerWalletException(HttpStatus.FORBIDDEN, "FORBIDDEN",
                    "Chỉ Organizer đang hoạt động mới có quyền truy cập");
        }

        List<Event> events = eventRepository.findByOrganizerIdOrderByIdAsc(organizerId);

        List<EventRevenueResponse> eventRevenues = new ArrayList<>();

        for (Event event : events) {
            List<TicketType> ticketTypes = ticketTypeRepository.findByEventIdOrderByIdAsc(event.getId());

            int eventSoldTickets = 0;
            int eventTotalTickets = 0;
            BigDecimal eventRevenue = financeLedger.eventRevenue(event.getId());

            for (TicketType tt : ticketTypes) {
                int quantity = tt.getQuantity() != null ? tt.getQuantity() : 0;
                int remaining = tt.getRemainingQuantity() != null ? tt.getRemainingQuantity() : quantity;
                int reserved = tt.getReservedQuantity() != null ? tt.getReservedQuantity() : 0;
                int sold = Math.max(0, quantity - remaining - reserved);

                eventSoldTickets += sold;
                eventTotalTickets += quantity;
            }

            if (eventSoldTickets > 0 || eventRevenue.signum() > 0) {
                BigDecimal finalRevenue = eventRevenue.setScale(2, RoundingMode.HALF_UP);
                eventRevenues.add(new EventRevenueResponse(
                        event.getId(),
                        event.getName(),
                        finalRevenue,
                        formatCurrency(finalRevenue),
                        eventSoldTickets,
                        eventTotalTickets
                ));
            }
        }

        // Lấy thông tin ví của Nhà tổ chức
        OrganizerWallet wallet = organizerWalletRepository.findByOrganizerId(organizerId)
                .orElseGet(() -> {
                    OrganizerWallet newWallet = new OrganizerWallet();
                    newWallet.setOrganizer(organizer);
                    newWallet.setPendingBalance(BigDecimal.ZERO);
                    newWallet.setAvailableBalance(BigDecimal.ZERO);
                    newWallet.setWithdrawalPendingBalance(BigDecimal.ZERO);
                    newWallet.setTotalWithdrawn(BigDecimal.ZERO);
                    return newWallet;
                });

        BigDecimal pendingBalance = (wallet.getPendingBalance() != null ? wallet.getPendingBalance() : BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal availableBalance = (wallet.getAvailableBalance() != null ? wallet.getAvailableBalance() : BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal withdrawalPendingBalance = (wallet.getWithdrawalPendingBalance() != null ? wallet.getWithdrawalPendingBalance() : BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalWithdrawn = (wallet.getTotalWithdrawn() != null ? wallet.getTotalWithdrawn() : BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        return new OrganizerWalletResponse(
                pendingBalance,
                formatCurrency(pendingBalance),
                availableBalance,
                formatCurrency(availableBalance),
                withdrawalPendingBalance,
                formatCurrency(withdrawalPendingBalance),
                totalWithdrawn,
                formatCurrency(totalWithdrawn),
                eventRevenues
        );
    }

    public static String formatCurrency(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return "0đ";
        }
        DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(Locale.of("vi", "VN"));
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        formatter.setDecimalFormatSymbols(symbols);
        formatter.setGroupingSize(3);
        formatter.setMaximumFractionDigits(0);
        return formatter.format(amount) + "đ";
    }
}
