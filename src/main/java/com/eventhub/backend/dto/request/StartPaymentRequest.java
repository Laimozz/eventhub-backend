package com.eventhub.backend.dto.request;

import jakarta.validation.Valid;

public record StartPaymentRequest(@Valid PurchaseBankAccountRequest bankAccount) {}
