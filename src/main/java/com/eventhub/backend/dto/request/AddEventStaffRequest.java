package com.eventhub.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Locale;

public record AddEventStaffRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 255, message = "Email tối đa 255 ký tự")
        String email
) {
    public AddEventStaffRequest {
        email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    }
}
