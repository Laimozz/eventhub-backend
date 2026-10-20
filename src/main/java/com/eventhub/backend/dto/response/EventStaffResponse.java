package com.eventhub.backend.dto.response;

import java.time.LocalDateTime;

public record EventStaffResponse(
        Integer id,
        Integer staffId,
        String fullName,
        String email,
        String phone,
        String status,
        boolean inCheckinShift,
        LocalDateTime createdAt
) {}
