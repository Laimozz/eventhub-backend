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
    @Mock PaymentRepository payments;
    @Mock EventRepository events;
    @Mock FinanceLedgerRepository ledger;
    @Mock FinanceLedgerService finance;
    @Mock NotificationService notifications;
    @Mock VnPayService vnpay;
    Clock clock=Clock.fixed(Instant.parse("2026-10-22T00:00:00Z"),ZoneOffset.UTC);
    RefundWorkflowService service;
    Refund refund;
    Payment payment;
    @BeforeEach void setup() {
        service=new RefundWorkflowService(workflow,refunds,payments,events,ledger,finance,notifications,vnpay,clock);
        var event=new Event(); event.setId(3); event.setStatus(EventStatus.CANCELED);
        var booking=new Booking(); booking.setId(2); booking.setEvent(event); booking.setStatus("PAID"); booking.setUser(new User());
        payment=new Payment(); payment.setId(1); payment.setBooking(booking); payment.setAmount(new BigDecimal("100000")); payment.setStatus("SUCCESS"); payment.setPaymentMethod("VNPAY");
        refund=new Refund(); refund.setId(4); refund.setPayment(payment); refund.setAmount(payment.getAmount()); refund.setStatus("WAITING");
        when(workflow.eventId(1)).thenReturn(Optional.of(3)); when(events.findForUpdate(3)).thenReturn(Optional.of(event));
        when(refunds.findByPaymentId(1)).thenReturn(Optional.of(refund));
    }
    @Test void preventsManualTransferWhileProviderOutcomeIsUnknown() {
        refund.setStatus("RECONCILIATION"); refund.setMethod("VNPAY");
        assertThatThrownBy(() -> service.reserveManual(1,10)).isInstanceOf(AdminException.class);
        verifyNoInteractions(finance,notifications);
    }
    @Test void refusesAutomaticTransferAfterManualReservation() {
        refund.setStatus("MANUAL_PENDING");
        assertThatThrownBy(() -> service.queue(1,10)).isInstanceOf(AdminException.class);
        verify(refunds,never()).saveAndFlush(any());
    }
    @Test void timeoutRequiresReconciliationWithoutReversingMoney() {
        refund.setStatus("PROCESSING");
        service.finish(new RefundWorkflowService.Work(payment,"attempt1",10,false),null);
        assertThat(refund.getStatus()).isEqualTo("RECONCILIATION"); verifyNoInteractions(finance,notifications);
    }
    @Test void completesAndNotifiesOnlyOnceAcrossDuplicateResponses() {
        refund.setStatus("PROCESSING");
        var work=new RefundWorkflowService.Work(payment,"attempt1",10,false);
        var result=new VnPayService.ApiResult(true,false,"00","00","123");
        service.finish(work,result); service.finish(work,result);
        assertThat(refund.getStatus()).isEqualTo("COMPLETED"); verify(finance,times(1)).reverse(payment);
        verify(notifications,times(1)).create(any(),anyString(),anyString(),eq("REFUND_COMPLETED"));
    }
    @Test void cannotQueueAlreadySettledMoney() {
        when(ledger.entry(1)).thenReturn(new FinanceLedgerRepository.Entry(new BigDecimal("95000"),true,true,false));
        assertThatThrownBy(() -> service.queue(1,10)).isInstanceOf(AdminException.class);
        assertThat(refund.getStatus()).isEqualTo("WAITING");
    }
}
