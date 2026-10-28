package com.eventhub.backend.service;

import com.eventhub.backend.dto.response.OrganizerReportResponse;
import com.eventhub.backend.dto.response.OrganizerReportResponse.EventFilterOptionResponse;

import java.time.LocalDate;
import java.util.List;

public interface OrganizerReportService {

    OrganizerReportResponse getReports(
            Integer organizerId,
            Integer eventId,
            String timeRange,
            LocalDate startDate,
            LocalDate endDate
    );

    List<EventFilterOptionResponse> getEventFilterOptions(Integer organizerId);

    byte[] exportReportCsv(
            Integer organizerId,
            Integer eventId,
            String timeRange,
            LocalDate startDate,
            LocalDate endDate
    );
}
