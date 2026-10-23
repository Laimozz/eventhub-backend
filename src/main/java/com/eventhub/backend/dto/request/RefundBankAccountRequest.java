package com.eventhub.backend.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RefundBankAccountRequest(
        @NotBlank @Size(max = 50) String bankCode,
        @NotBlank @Pattern(regexp = "[0-9A-Za-z]{4,50}") String accountNumber,
        @NotBlank @Size(max = 255) String accountHolderName,
        @AssertTrue(message = "Phải xác minh đây là tài khoản thanh toán gốc") boolean originalAccountVerified) {}
