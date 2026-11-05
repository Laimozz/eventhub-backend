package com.eventhub.backend.service;

/** Only controlled diagnostics may be persisted or shown to the administrator. */
public record RefundProviderFailure(String code, String stage, String message, Integer httpStatus) {
    public static RefundProviderFailure classify(RuntimeException error) {
        if (error instanceof VerificationException verification) return verification.failure;
        if (error instanceof PreparationException preparation) return preparation.failure;
        if (error instanceof org.springframework.web.client.RestClientResponseException http)
            return new RefundProviderFailure("PROVIDER_HTTP_ERROR", "HTTP", "VNPAY trả lỗi HTTP; chưa xác minh kết quả hoàn tiền.", http.getStatusCode().value());
        if (error instanceof org.springframework.web.client.ResourceAccessException)
            return new RefundProviderFailure("PROVIDER_CONNECTION_ERROR", "HTTP", "Không nhận được phản hồi VNPAY do lỗi kết nối hoặc hết thời gian chờ.", null);
        if (error instanceof org.springframework.web.client.RestClientException)
            return new RefundProviderFailure("PROVIDER_RESPONSE_UNREADABLE", "RESPONSE", "Không đọc được phản hồi VNPAY; cần đối soát.", null);
        return new RefundProviderFailure("PROVIDER_INTERNAL_ERROR", "INTERNAL", "Lỗi nội bộ khi xử lý yêu cầu VNPAY; cần kiểm tra mã chẩn đoán.", null);
    }

    public static class PreparationException extends RuntimeException {
        private final RefundProviderFailure failure;
        public PreparationException() {
            super("PROVIDER_INPUT_INVALID");
            failure = new RefundProviderFailure("PROVIDER_INPUT_INVALID", "PREPARE",
                    "Không gửi được yêu cầu VNPAY vì dữ liệu giao dịch chưa đầy đủ hoặc không hợp lệ.", null);
        }
    }

    public static class VerificationException extends RuntimeException {
        private final RefundProviderFailure failure;
        public VerificationException(String code, String message) {
            super(code);
            failure = new RefundProviderFailure(code, "VERIFY", message, null);
        }
    }
}
