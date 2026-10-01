package com.eventhub.backend.dto.response;

import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.Role;

public record UserResponse(Integer id, String email, String fullName, String phone,
        Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getPhone(),
                user.getRole());
    }
}
