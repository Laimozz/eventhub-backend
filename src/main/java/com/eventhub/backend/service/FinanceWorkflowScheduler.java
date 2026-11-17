package com.eventhub.backend.service;

import com.eventhub.backend.repository.FinanceLedgerRepository;
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
    private final Clock clock;

    @Scheduled(fixedDelayString="${app.finance.worker-delay:30s}",initialDelayString="${app.finance.worker-delay:30s}")
    public void process() {
        for(Integer eventId:ledger.settlementEvents(LocalDateTime.now(clock))) {
            try { finance.settleEvent(eventId); }
            catch(RuntimeException ex) { log.warn("Settlement requires review for event {} ({})",eventId,ex.getClass().getSimpleName()); }
        }
    }
}
