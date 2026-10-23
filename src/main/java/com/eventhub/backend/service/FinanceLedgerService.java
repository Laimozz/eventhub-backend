package com.eventhub.backend.service;

import com.eventhub.backend.entity.Payment;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.exception.AdminException;
import com.eventhub.backend.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FinanceLedgerService {
    private final FinanceLedgerRepository ledger;
    private final OrganizerWalletRepository wallets;
    private final EventRepository events;
    private final Clock clock;
    private final RefundWorkflowRepository refundWorkflow;

    // Caller holds the event lock; the payment and issued tickets belong to the same transaction.
    public void recordPayment(Payment payment) {
        if (!"SUCCESS".equals(payment.getStatus())) return;
        BigDecimal gross = payment.getAmount();
        BigDecimal fee = gross.multiply(new BigDecimal("0.05")).setScale(0, RoundingMode.HALF_UP);
        boolean credited = "PAID".equals(payment.getBooking().getStatus());
        boolean recorded = ledger.record(payment.getId(), gross, fee, credited);
        if (payment.getBooking().getEvent().getStatus() == EventStatus.CANCELED
                || "PAYMENT_REVIEW".equals(payment.getBooking().getStatus())) refundWorkflow.seedPayment(payment.getId());
        if (!recorded || !credited) return;
        Integer organizerId = payment.getBooking().getEvent().getOrganizer().getId();
        ledger.ensureWallet(organizerId);
        var wallet = wallets.findByOrganizerIdForUpdate(organizerId).orElseThrow();
        wallet.setPendingBalance(wallet.getPendingBalance().add(gross.subtract(fee)));
        wallets.saveAndFlush(wallet);
    }

    public void settleEvent(Integer eventId) {
        var event = events.findForUpdate(eventId).orElseThrow();
        if (event.getEndTime().isAfter(LocalDateTime.now(clock))
                || !(event.getStatus() == EventStatus.APPROVED || event.getStatus() == EventStatus.ONGOING
                || event.getStatus() == EventStatus.COMPLETED)) return;
        for (Integer paymentId : ledger.releasablePayments(eventId)) {
            var entry = ledger.entry(paymentId);
            var wallet = wallets.findByOrganizerIdForUpdate(event.getOrganizer().getId()).orElseThrow();
            if (wallet.getPendingBalance().compareTo(entry.organizerAmount()) < 0) throw discrepancy();
            wallet.setPendingBalance(wallet.getPendingBalance().subtract(entry.organizerAmount()));
            wallet.setAvailableBalance(wallet.getAvailableBalance().add(entry.organizerAmount()));
            wallets.saveAndFlush(wallet);
            ledger.settle(paymentId);
        }
    }

    public void reverse(Payment payment) {
        var entry = ledger.entry(payment.getId());
        if (entry.reversed()) return;
        // A late error after settlement must be reconciled; never debit unrelated available funds.
        if (entry.settled()) throw discrepancy();
        if (entry.credited()) {
            var wallet = wallets.findByOrganizerIdForUpdate(payment.getBooking().getEvent().getOrganizer().getId()).orElseThrow();
            if (wallet.getPendingBalance().compareTo(entry.organizerAmount()) < 0) throw discrepancy();
            wallet.setPendingBalance(wallet.getPendingBalance().subtract(entry.organizerAmount()));
            wallets.saveAndFlush(wallet);
        }
        ledger.reverse(payment.getId());
    }

    private AdminException discrepancy() {
        return new AdminException(HttpStatus.CONFLICT, "FINANCE_REQUIRES_RECONCILIATION", "Nguồn tiền cần Admin đối soát trước khi xử lý.");
    }
}
