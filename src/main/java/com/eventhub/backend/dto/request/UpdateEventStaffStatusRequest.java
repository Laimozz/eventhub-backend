package com.eventhub.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateEventStaffStatusRequest(
        @NotBlank(message = "Trạng thái không được để trống")
        @Pattern(regexp = "ACTIVE|LOCKED|INACTIVE", message = "Trạng thái phải là ACTIVE hoặc LOCKED")
        String status
) {
    public UpdateEventStaffStatusRequest {
        if (status != null) {
            String s = status.strip().toUpperCase();
            if ("INACTIVE".equals(s)) {
                status = "LOCKED";
            } else {
                status = s;
            }
        }
    }
}
