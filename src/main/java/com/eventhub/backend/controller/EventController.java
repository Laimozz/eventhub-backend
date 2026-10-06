package com.eventhub.backend.controller;

import com.eventhub.backend.dto.request.CreateEventRequest;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService events;
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventResponse> createEvent(@AuthenticationPrincipal UserResponse user,
            @Valid @RequestPart("event") CreateEventRequest request,
            @RequestParam Map<String, MultipartFile> files) {
        var eventFiles = new LinkedHashMap<>(files);
        eventFiles.remove("event");
        return ResponseEntity.status(HttpStatus.CREATED).body(events.createEvent(user.id(), request, eventFiles));
    }

}
