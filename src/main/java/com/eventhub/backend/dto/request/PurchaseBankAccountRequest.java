package com.eventhub.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// A customer's declaration is not proof of ownership or of the payment source.
public record PurchaseBankAccountRequest(
        @NotBlank @Size(max = 50) String bankCode,
        @NotBlank @Pattern(regexp = "[0-9A-Za-z]{4,50}") String accountNumber,
        @NotBlank @Size(max = 255) String accountHolderName) {}
