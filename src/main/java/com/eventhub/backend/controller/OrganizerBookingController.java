package com.eventhub.backend.controller;

import com.eventhub.backend.dto.response.OrganizerBookingResponses.OrganizerBookingDetail;
import com.eventhub.backend.dto.response.OrganizerBookingResponses.OrganizerBookingPageResponse;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.service.OrganizerBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/organizer/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class OrganizerBookingController {

    private final OrganizerBookingService bookingService;

    @GetMapping
    public ResponseEntity<OrganizerBookingPageResponse> getBookings(
            @AuthenticationPrincipal UserResponse user,
            @RequestParam(required = false) Integer eventId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(bookingService.getBookings(
                user.id(), eventId, status, keyword, startDate, endDate, page, size));
    }

    @GetMapping("/{bookingId:\\d+}")
    public ResponseEntity<OrganizerBookingDetail> getBookingDetail(
            @AuthenticationPrincipal UserResponse user,
            @PathVariable Integer bookingId
    ) {
        return ResponseEntity.ok(bookingService.getBookingDetail(user.id(), bookingId));
    }

    @GetMapping("/export-csv")
    public ResponseEntity<byte[]> exportBookingsCsv(
            @AuthenticationPrincipal UserResponse user,
            @RequestParam(required = false) Integer eventId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        byte[] csv = bookingService.exportBookingsCsv(
                user.id(), eventId, status, keyword, startDate, endDate);
        String filename = "danh-sach-don-hang-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
