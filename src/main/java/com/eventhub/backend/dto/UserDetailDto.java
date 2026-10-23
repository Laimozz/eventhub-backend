package com.eventhub.backend.dto;

import java.time.LocalDate;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserDetailDto(
        Integer id,
        @NotBlank(message = "Họ và tên không được để trống")
        @Size(max = 255, message = "Họ và tên không quá 255 ký tự")
        String fullName,
        @NotBlank(message = "Email không được để trống")
        @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Email không đúng định dạng (ví dụ: example@domain.com)")
        @Size(max = 255, message = "Email không quá 255 ký tự")
        String email,
        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^(?:\\+84|0)[35789][0-9]{8}$", message = "Số điện thoại không đúng định dạng (gồm 10 chữ số, ví dụ: 0908123456)")
        @Size(max = 20, message = "Số điện thoại không quá 20 ký tự")
        String phone,
        Role role,
        String status,
        @Past(message = "Ngày sinh phải trước ngày hiện tại")
        LocalDate dateOfBirth,
        String gender,
        String avatarUrl,
        String city,
        String preferredCategories
) {
    public UserDetailDto {
        email = email == null ? null : email.strip().toLowerCase();
        fullName = fullName == null ? null : fullName.strip();
        phone = phone == null ? null : phone.strip().replaceAll("[\\s.-]+", "");
        city = city == null ? null : city.strip();
        preferredCategories = preferredCategories == null ? null : preferredCategories.strip();
    }

    public UserDetailDto(
            Integer id,
            String fullName,
            String email,
            String phone,
            Role role,
            String status,
            LocalDate dateOfBirth,
            String gender,
            String avatarUrl
    ) {
        this(id, fullName, email, phone, role, status, dateOfBirth, gender, avatarUrl, null, null);
    }

    public static UserDetailDto from(User user) {
        return new UserDetailDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.getDateOfBirth(),
                user.getGender(),
                user.getAvatarUrl(),
                user.getCity(),
                user.getPreferredCategories()
        );
    }
}
