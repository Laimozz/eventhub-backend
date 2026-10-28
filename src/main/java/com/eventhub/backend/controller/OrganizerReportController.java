package com.eventhub.backend.controller;

import com.eventhub.backend.dto.response.OrganizerReportResponse;
import com.eventhub.backend.dto.response.OrganizerReportResponse.EventFilterOptionResponse;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.service.OrganizerReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/organizer/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class OrganizerReportController {

    private final OrganizerReportService reportService;

    @GetMapping
    public ResponseEntity<OrganizerReportResponse> getReports(
            @AuthenticationPrincipal UserResponse user,
            @RequestParam(required = false) Integer eventId,
            @RequestParam(defaultValue = "30d") String timeRange,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(reportService.getReports(user.id(), eventId, timeRange, startDate, endDate));
    }

    @GetMapping("/events-filter")
    public ResponseEntity<List<EventFilterOptionResponse>> getEventFilterOptions(
            @AuthenticationPrincipal UserResponse user
    ) {
        return ResponseEntity.ok(reportService.getEventFilterOptions(user.id()));
    }

    @GetMapping("/export-csv")
    public ResponseEntity<byte[]> exportReportCsv(
            @AuthenticationPrincipal UserResponse user,
            @RequestParam(required = false) Integer eventId,
            @RequestParam(defaultValue = "30d") String timeRange,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        byte[] csvData = reportService.exportReportCsv(user.id(), eventId, timeRange, startDate, endDate);
        String filename = "bao-cao-thong-ke-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }
}
