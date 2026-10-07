package com.eventhub.backend.controller;

import com.eventhub.backend.dto.request.CreateEventRequest;
import com.eventhub.backend.dto.request.UpdateEventRequest;
import com.eventhub.backend.dto.request.CancelEventRequest;
import com.eventhub.backend.dto.response.EventListResponse;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.dto.response.EventResponse;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.service.EventService;
import java.util.Map;
import java.util.LinkedHashMap;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ORGANIZER')")
public class EventController {
    private final EventService events;

    @GetMapping("/mine")
    public EventListResponse listEvents(@AuthenticationPrincipal UserResponse user,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "") String search, @RequestParam(required = false) EventStatus status) {
        return events.listEvents(user.id(), page, size, search, status);
    }

    @GetMapping("/{eventId:\\d+}")
    public EventResponse getEvent(@AuthenticationPrincipal UserResponse user, @PathVariable Integer eventId) {
        return events.getEvent(user.id(), eventId);
    }

    @PutMapping(value = "/{eventId:\\d+}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EventResponse updateEvent(@AuthenticationPrincipal UserResponse user, @PathVariable Integer eventId,
            @Valid @RequestPart("event") UpdateEventRequest request, @RequestParam Map<String, MultipartFile> files) {
        var eventFiles = new LinkedHashMap<>(files);
        eventFiles.remove("event");
        return events.updateEvent(user.id(), eventId, request, eventFiles);
    }

    @PostMapping("/{eventId:\\d+}/cancel")
    public EventResponse cancelEvent(@AuthenticationPrincipal UserResponse user, @PathVariable Integer eventId,
            @Valid @RequestBody CancelEventRequest request) {
        return events.cancelEvent(user.id(), eventId, request.reason());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventResponse> createEvent(@AuthenticationPrincipal UserResponse user,
            @Valid @RequestPart("event") CreateEventRequest request,
            @RequestParam Map<String, MultipartFile> files) {
        var eventFiles = new LinkedHashMap<>(files);
        eventFiles.remove("event");
        return ResponseEntity.status(HttpStatus.CREATED).body(events.createEvent(user.id(), request, eventFiles));
    }

}
