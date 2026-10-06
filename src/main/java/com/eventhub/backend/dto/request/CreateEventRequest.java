package com.eventhub.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CreateEventRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 255) String description,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        @NotNull @Positive Integer categoryId,
        @NotNull @Valid VenueRequest venue,
        @NotEmpty List<@NotNull @Valid TicketTypeRequest> ticketTypes,
        List<@NotNull @Valid GuestRequest> guests) {

    public record VenueRequest(
            @NotBlank @Size(max = 255) String city,
            @NotBlank @Size(max = 255) String address,
            @NotNull @Positive Integer capacity) {
    }

    public record TicketTypeRequest(
            @NotBlank @Size(max = 255) String name,
            @Size(max = 255) String description,
            @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal price,
            @NotNull @Positive Integer quantity,
            @NotNull LocalDateTime saleStartTime,
            @NotNull LocalDateTime saleEndTime) {
    }

    public record GuestRequest(
            @NotBlank @Size(max = 50) String name,
            @NotBlank @Size(max = 50) String role,
            @Size(max = 255) String description) {
    }
}
