package com.eventhub.backend.controller;

import com.eventhub.backend.dto.response.AdminFinanceResponses.*;
import com.eventhub.backend.dto.response.AdminResponses.PageResponse;
import com.eventhub.backend.service.AdminFinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminFinanceController {
    private final AdminFinanceService finance;

    @GetMapping("/statistics")
    public OverviewResponse overview() { return finance.overview(); }

    @GetMapping("/statistics/monthly")
    public List<AdminMonthlyTrendResponse> monthlyStats(@RequestParam(defaultValue = "6") int months) {
        return finance.monthlyStats(months);
    }

    @GetMapping("/statistics/status-breakdown")
    public List<AdminStatusBreakdownResponse> statusBreakdown() {
        return finance.eventStatusBreakdown();
    }

    @GetMapping("/statistics/category-breakdown")
    public List<AdminCategoryRevenueResponse> categoryBreakdown() {
        return finance.categoryRevenue();
    }

    @GetMapping("/refund-events")
    public PageResponse<CanceledEventResponse> canceledEvents(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) { return finance.canceledEvents(page, pageSize); }

    @GetMapping("/refund-events/{eventId}")
    public RefundDetailResponse detail(@PathVariable Integer eventId, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) { return finance.refundDetail(eventId, page, pageSize); }

    @GetMapping("/statistics/refund-reconciliation/{eventId}")
    public RefundReconciliationResponse refundReconciliation(@PathVariable Integer eventId) {
        return finance.getEventRefundReconciliation(eventId);
    }
}

