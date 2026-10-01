package com.eventhub.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 255) String fullName,
        @Size(max = 20) String phone,
        @NotBlank @Pattern(regexp = "CUSTOMER|ORGANIZER", message = "Role must be CUSTOMER or ORGANIZER") String role) {

    public RegisterRequest {
        email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
        fullName = fullName == null ? null : fullName.strip();
        phone = phone == null ? null : phone.strip();
    }

    @JsonIgnore
    @AssertTrue(message = "Password must not exceed 72 UTF-8 bytes")
    public boolean isPasswordWithinByteLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    @Override
    public String toString() {
        return "RegisterRequest[credentials=REDACTED]";
    }
}
