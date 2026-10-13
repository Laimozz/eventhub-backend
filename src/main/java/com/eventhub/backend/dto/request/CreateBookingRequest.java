package com.eventhub.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateBookingRequest(
        @NotNull @Positive Integer eventId,
        @NotEmpty @Size(max = 20) List<@NotNull @Valid Item> items) {
    public record Item(@NotNull @Positive Integer ticketTypeId,
                       @NotNull @Min(1) @Max(100) Integer quantity) {}
}
