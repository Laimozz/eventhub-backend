package com.eventhub.backend.service;

import com.eventhub.backend.dto.response.OrganizerBookingResponses.OrganizerBookingDetail;
import com.eventhub.backend.dto.response.OrganizerBookingResponses.OrganizerBookingPageResponse;

import java.time.LocalDate;

public interface OrganizerBookingService {

    OrganizerBookingPageResponse getBookings(
            Integer organizerId,
            Integer eventId,
            String status,
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    );

    OrganizerBookingDetail getBookingDetail(
            Integer organizerId,
            Integer bookingId
    );

    byte[] exportBookingsCsv(
            Integer organizerId,
            Integer eventId,
            String status,
            String keyword,
            LocalDate startDate,
            LocalDate endDate
    );
}
