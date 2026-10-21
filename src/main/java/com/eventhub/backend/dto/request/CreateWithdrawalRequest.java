package com.eventhub.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateWithdrawalRequest(
        @NotNull(message = "Số tiền không được để trống")
        @DecimalMin(value = "0", inclusive = false, message = "Số tiền phải lớn hơn 0")
        BigDecimal amount,

        @NotBlank(message = "Tên ngân hàng không được để trống")
        @Size(max = 255, message = "Tên ngân hàng tối đa 255 ký tự")
        String bankName,

        @NotBlank(message = "Số tài khoản không được để trống")
        @Size(max = 50, message = "Số tài khoản tối đa 50 ký tự")
        String accountNumber
) {
    public CreateWithdrawalRequest {
        bankName = bankName == null ? null : bankName.strip();
        accountNumber = accountNumber == null ? null : accountNumber.strip();
    }
}
