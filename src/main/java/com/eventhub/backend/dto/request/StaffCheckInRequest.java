package com.eventhub.backend.dto.request;

public record StaffCheckInRequest(
        Integer ticketId,
        String ticketCode
) {}
