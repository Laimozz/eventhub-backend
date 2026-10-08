package com.eventhub.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class AdminRequests {
    private AdminRequests() {}

    public record CreateUserRequest(
            @NotBlank @Size(max = 255) String fullName,
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(max = 20) String phone,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank @Pattern(regexp = "CUSTOMER|ORGANIZER") String role) {
        public CreateUserRequest {
            fullName = strip(fullName);
            email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
            phone = strip(phone);
        }
        @JsonIgnore
        @AssertTrue(message = "Password must not exceed 72 UTF-8 bytes")
        public boolean isPasswordWithinByteLimit() {
            return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
        }
        @Override public String toString() { return "CreateUserRequest[credentials=REDACTED]"; }
    }

    public record UpdateUserStatusRequest(@NotBlank @Pattern(regexp = "ACTIVE|LOCKED") String status) {}
    public record SaveCategoryRequest(
            @NotBlank @Size(max = 255) String name,
            @NotBlank @Size(max = 255) String description) {
        public SaveCategoryRequest { name = strip(name); description = strip(description); }
    }
    public record ApproveEventRequest(@NotNull @PositiveOrZero Long version) {}
    public record RejectEventRequest(@NotNull @PositiveOrZero Long version,
            @NotBlank @Size(max = 255) String reason) {
        public RejectEventRequest { reason = strip(reason); }
    }
    private static String strip(String value) { return value == null ? null : value.strip(); }
}
