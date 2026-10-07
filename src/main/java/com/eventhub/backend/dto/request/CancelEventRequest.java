package com.eventhub.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelEventRequest(@NotBlank @Size(max = 255) String reason) {
}
