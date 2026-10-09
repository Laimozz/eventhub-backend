package com.eventhub.backend.controller;

import com.eventhub.backend.dto.response.PublicEventDetailResponse;
import com.eventhub.backend.dto.response.PublicEventSummaryResponse;
import com.eventhub.backend.service.EventService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService events;

    @GetMapping
    public com.eventhub.backend.dto.response.PublicEventListResponse listEvents(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size) {
        return events.listPublicEvents(categoryId, city, fromDate, toDate, search, page, size);
    }

    @GetMapping("/{eventId:\\d+}")
    public PublicEventDetailResponse getEvent(@PathVariable Integer eventId) {
        return events.getPublicEvent(eventId);
    }
}
