package com.eventhub.backend.service;

import com.eventhub.backend.entity.*;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.exception.AdminException;
import com.eventhub.backend.repository.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundWorkflowServiceTest {
    @Mock RefundWorkflowRepository workflow;
    @Mock RefundRepository refunds;
    @Mock EventRepository events;
    @Mock FinanceLedgerRepository ledger;
    @Mock FinanceLedgerService finance;
    @Mock NotificationService notifications;
    Clock clock=Clock.fixed(Instant.parse("2026-10-22T00:00:00Z"),ZoneOffset.UTC);
    RefundWorkflowService service;
    Refund refund;
    Payment payment;
    @BeforeEach void setup() {
        service=new RefundWorkflowService(workflow,refunds,events,ledger,finance,notifications,clock);
        var event=new Event(); event.setId(3); event.setStatus(EventStatus.CANCELED);
        var booking=new Booking(); booking.setId(2); booking.setEvent(event); booking.setStatus("PAID"); booking.setUser(new User());
        payment=new Payment(); payment.setId(1); payment.setBooking(booking); payment.setAmount(new BigDecimal("100000")); payment.setStatus("SUCCESS"); payment.setPaymentMethod("VNPAY");
        refund=new Refund(); refund.setId(4); refund.setPayment(payment); refund.setAmount(payment.getAmount()); refund.setStatus("WAITING");
        when(workflow.eventId(1)).thenReturn(Optional.of(3)); when(events.findForUpdate(3)).thenReturn(Optional.of(event));
        when(refunds.findByPaymentId(1)).thenReturn(Optional.of(refund));
    }
    @Test void completesAndNotifiesOnlyOnce() {
        when(ledger.entry(1)).thenReturn(new FinanceLedgerRepository.Entry(new BigDecimal("95000"),true,false,false));
        var first = service.complete(1,10);
        var second = service.complete(1,10);
        assertThat(first.status()).isEqualTo("COMPLETED");
        assertThat(first.reference()).startsWith("RF-").isEqualTo(second.reference());
        verify(finance,times(1)).reverse(payment);
        verify(notifications,times(1)).create(any(),eq("Hoàn tiền thành công"),anyString(),eq("REFUND_COMPLETED"));
    }
    @Test void rejectsSettledMoney() {
        when(ledger.entry(1)).thenReturn(new FinanceLedgerRepository.Entry(new BigDecimal("95000"),true,true,false));
        assertThatThrownBy(() -> service.complete(1,10)).isInstanceOf(AdminException.class);
        assertThat(refund.getStatus()).isEqualTo("WAITING");
        verifyNoInteractions(finance,notifications);
    }
    @Test void rejectsUnpaidPayment() {
        payment.setStatus("FAILED");
        assertThatThrownBy(() -> service.complete(1,10)).isInstanceOf(AdminException.class);
        verifyNoInteractions(finance,notifications);
    }
    @Test void rejectsActiveEvent() {
        payment.getBooking().getEvent().setStatus(EventStatus.APPROVED);
        assertThatThrownBy(() -> service.complete(1,10)).isInstanceOf(AdminException.class);
        verifyNoInteractions(finance,notifications);
    }
}
