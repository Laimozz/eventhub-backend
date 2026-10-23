package com.eventhub.backend.service;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefundProviderService {
    private final RefundWorkflowService workflow;
    private final VnPayService provider;
    private final Clock clock;

    // Claim and finish commit separately. No DB transaction is held during the HTTP request.
    public void execute(Integer paymentId,boolean query) {
        var work=workflow.claim(paymentId,query);
        if(work==null) return;
        VnPayService.ApiResult result;
        try {
            result=query?provider.queryRefund(work.payment(),work.requestId(),LocalDateTime.now(clock))
                    :provider.requestRefund(work.payment(),work.requestId(),work.adminId(),LocalDateTime.now(clock));
        } catch(RuntimeException ex) {
            workflow.finish(work,null);
            return;
        }
        // A commit failure must not be mistaken for a provider rejection or trigger another refund.
        workflow.finish(work,result);
    }
}
