package com.eventhub.backend.service;

import com.eventhub.backend.repository.FinanceLedgerRepository;
import com.eventhub.backend.repository.RefundWorkflowRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name="app.finance.worker-enabled",havingValue="true",matchIfMissing=true)
public class FinanceWorkflowScheduler {
    private final FinanceLedgerRepository ledger;
    private final FinanceLedgerService finance;
    private final RefundWorkflowRepository workflow;
    private final RefundWorkflowService refunds;
    private final RefundProviderService provider;
    private final Clock clock;

    @Scheduled(fixedDelayString="${app.finance.worker-delay:30s}",initialDelayString="${app.finance.worker-delay:30s}")
    public void process() {
        for(Integer eventId:ledger.settlementEvents(LocalDateTime.now(clock))) {
            try { finance.settleEvent(eventId); }
            catch(RuntimeException ex) { log.warn("Settlement requires review for event {} ({})",eventId,ex.getClass().getSimpleName()); }
        }
        for(Integer paymentId:workflow.interrupted(LocalDateTime.now(clock).minusMinutes(10))) refunds.recover(paymentId);
        for(Integer paymentId:workflow.queued()) {
            try { provider.execute(paymentId,false); }
            catch(RuntimeException ex) { log.warn("Refund requires review for payment {} ({})",paymentId,ex.getClass().getSimpleName()); }
        }
    }
}
