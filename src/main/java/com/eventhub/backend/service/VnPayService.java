package com.eventhub.backend.service;

import com.eventhub.backend.config.ApplicationProperties;
import com.eventhub.backend.entity.Payment;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VnPayService {
    private static final ZoneId VNPAY_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("uuuuMMddHHmmss")
            .withResolverStyle(ResolverStyle.STRICT);
    private final ApplicationProperties properties;
    private final org.springframework.web.client.RestClient apiClient;

    @org.springframework.beans.factory.annotation.Autowired
    public VnPayService(ApplicationProperties properties) {
        this(properties, defaultApiClient());
    }

    public VnPayService(ApplicationProperties properties, org.springframework.web.client.RestClient apiClient) {
        this.properties = properties;
        this.apiClient = apiClient;
    }

    private static org.springframework.web.client.RestClient defaultApiClient() {
        var http = java.net.http.HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(5)).build();
        var factory = new org.springframework.http.client.JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(java.time.Duration.ofSeconds(20));
        return org.springframework.web.client.RestClient.builder().requestFactory(factory).build();
    }

    public void requireApiConfiguration() {
        requireConfiguration();
        if (!validUrl(properties.getVnpay().getApiUrl(), true)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "VNPay API URL is not configured");
        }
    }

    public ApiResult requestRefund(RefundPaymentSnapshot payment, String requestId, Integer adminId, LocalDateTime now) {
        var fields = apiFields(payment, requestId, "refund", now);
        fields.put("vnp_TransactionType", "02");
        fields.put("vnp_Amount", amount(payment.amount()));
        fields.put("vnp_CreateBy", "admin-" + adminId);
        fields.put("vnp_SecureHash", sign(join(fields, "vnp_RequestId", "vnp_Version", "vnp_Command", "vnp_TmnCode",
                "vnp_TransactionType", "vnp_TxnRef", "vnp_Amount", "vnp_TransactionNo", "vnp_TransactionDate",
                "vnp_CreateBy", "vnp_CreateDate", "vnp_IpAddr", "vnp_OrderInfo")));
        return executeApi(fields, payment, false);
    }

    public ApiResult queryRefund(RefundPaymentSnapshot payment, String requestId, LocalDateTime now) {
        var fields = apiFields(payment, requestId, "querydr", now);
        fields.put("vnp_SecureHash", sign(join(fields, "vnp_RequestId", "vnp_Version", "vnp_Command", "vnp_TmnCode",
                "vnp_TxnRef", "vnp_TransactionDate", "vnp_CreateDate", "vnp_IpAddr", "vnp_OrderInfo")));
        return executeApi(fields, payment, true);
    }

    private Map<String, String> apiFields(RefundPaymentSnapshot payment, String requestId, String command, LocalDateTime now) {
        requireApiConfiguration();
        if (payment == null || payment.id() == null || payment.transactionId() == null
                || payment.transactionId().isBlank() || payment.createdAt() == null || payment.amount() == null
                || payment.amount().signum() <= 0) throw new RefundProviderFailure.PreparationException();
        var fields = new java.util.LinkedHashMap<String, String>();
        fields.put("vnp_RequestId", requestId);
        fields.put("vnp_Version", "2.1.0");
        fields.put("vnp_Command", command);
        fields.put("vnp_TmnCode", properties.getVnpay().getTmnCode());
        fields.put("vnp_TxnRef", payment.transactionId());
        fields.put("vnp_TransactionNo", payment.gatewayTransactionId() == null ? "" : payment.gatewayTransactionId());
        fields.put("vnp_TransactionDate", formatDate(payment.createdAt()));
        fields.put("vnp_CreateDate", formatDate(now));
        fields.put("vnp_IpAddr", properties.getVnpay().getApiIpAddress());
        fields.put("vnp_OrderInfo", "Refund payment " + payment.id());
        return fields;
    }

    private ApiResult executeApi(Map<String, String> fields, RefundPaymentSnapshot payment, boolean query) {
        var result = apiClient
                .post().uri(properties.getVnpay().getApiUrl()).contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(fields).retrieve().body(new org.springframework.core.ParameterizedTypeReference<Map<String, String>>() {});
        return verifyApiResponse(result, fields, payment, query);
    }

    ApiResult verifyApiResponse(Map<String, String> result, Map<String, String> fields, RefundPaymentSnapshot payment, boolean query) {
        if (result == null) throw new RefundProviderFailure.VerificationException("PROVIDER_EMPTY_RESPONSE", "VNPAY trả phản hồi rỗng; chưa xác minh kết quả.");
        String signed = join(result, "vnp_ResponseId", "vnp_Command", "vnp_ResponseCode", "vnp_Message", "vnp_TmnCode",
                "vnp_TxnRef", "vnp_Amount", "vnp_BankCode", "vnp_PayDate", "vnp_TransactionNo",
                "vnp_TransactionType", "vnp_TransactionStatus", "vnp_OrderInfo");
        if (query) signed += "|" + join(result,"vnp_PromotionCode","vnp_PromotionAmount");
        String supplied = result.get("vnp_SecureHash");
        if (supplied == null || supplied.isBlank())
            throw verificationFailure(result,"PROVIDER_SIGNATURE_MISSING","Phản hồi thiếu chữ ký vnp_SecureHash.",false);
        if (!supplied.matches("[a-fA-F0-9]{128}"))
            throw verificationFailure(result,"PROVIDER_SIGNATURE_INVALID_FORMAT","Chữ ký vnp_SecureHash không đúng định dạng HMAC-SHA512.",false);
        if (!MessageDigest.isEqual(HexFormat.of().parseHex(sign(signed)), HexFormat.of().parseHex(supplied)))
            throw verificationFailure(result,"PROVIDER_SIGNATURE_MISMATCH","Chữ ký phản hồi không khớp với chữ ký backend tính; cần kiểm tra khóa và chuỗi ký.",false);
        for (String key : java.util.List.of("vnp_TmnCode","vnp_Command","vnp_TxnRef","vnp_Amount",
                "vnp_ResponseCode","vnp_TransactionType","vnp_TransactionStatus","vnp_TransactionNo")) {
            if (result.get(key) == null || result.get(key).isBlank())
                throw verificationFailure(result,"PROVIDER_FIELD_MISSING","Phản hồi thiếu trường " + key + ".",true);
        }
        if (!properties.getVnpay().getTmnCode().equals(result.get("vnp_TmnCode")))
            throw verificationFailure(result,"PROVIDER_MERCHANT_MISMATCH","Merchant vnp_TmnCode không khớp cấu hình.",true);
        if (!fields.get("vnp_Command").equals(result.get("vnp_Command")))
            throw verificationFailure(result,"PROVIDER_COMMAND_MISMATCH","Loại lệnh vnp_Command không khớp yêu cầu.",true);
        if (!payment.transactionId().equals(result.get("vnp_TxnRef")))
            throw verificationFailure(result,"PROVIDER_REFERENCE_MISMATCH","Mã giao dịch vnp_TxnRef không khớp payment.",true);
        if (!amount(payment.amount()).equals(result.get("vnp_Amount")))
            throw verificationFailure(result,"PROVIDER_AMOUNT_MISMATCH","Số tiền vnp_Amount không khớp payment theo đơn vị VNPAY (VND nhân 100).",true);
        boolean refund = "02".equals(result.get("vnp_TransactionType"));
        String code = result.get("vnp_ResponseCode"), status = result.get("vnp_TransactionStatus");
        // Querying a successful original payment is not confirmation of a successful refund.
        boolean completed = refund && "00".equals(code) && "00".equals(status)
                && result.get("vnp_TransactionNo").matches("[1-9][0-9]{0,14}");
        return new ApiResult(completed, refund && "00".equals(code) && "09".equals(status),
                code, status, result.get("vnp_TransactionNo"));
    }

    private String join(Map<String, String> fields, String... names) {
        return java.util.Arrays.stream(names).map(name -> java.util.Objects.toString(fields.get(name), "")).collect(Collectors.joining("|"));
    }

    private RefundProviderFailure.VerificationException verificationFailure(Map<String,String> response,
            String diagnosticCode,String message,boolean signatureVerified) {
        // Error responses may omit their signature. Keep only a two-digit hint, never raw provider text.
        String code = response.get("vnp_ResponseCode");
        if (code != null && code.matches("[0-9]{2}"))
            message += " vnp_ResponseCode=" + code + (signatureVerified ? " (chữ ký hợp lệ)." : " (chưa xác minh).");
        return new RefundProviderFailure.VerificationException(diagnosticCode,message);
    }

    public record ApiResult(boolean completed, boolean rejected, String responseCode, String transactionStatus, String reference) {}

    public String createPaymentUrl(Payment payment, String ipAddress, LocalDateTime expiresAt) {
        requireConfiguration();
        var config = properties.getVnpay();
        var parameters = new TreeMap<String, String>();
        parameters.put("vnp_Version", "2.1.0");
        parameters.put("vnp_Command", "pay");
        parameters.put("vnp_TmnCode", config.getTmnCode());
        parameters.put("vnp_Amount", amount(payment.getAmount()));
        parameters.put("vnp_CurrCode", "VND");
        parameters.put("vnp_TxnRef", payment.getTransactionId());
        parameters.put("vnp_OrderInfo", "Thanh toan booking " + payment.getBooking().getId());
        parameters.put("vnp_OrderType", "other");
        parameters.put("vnp_Locale", "vn");
        parameters.put("vnp_ReturnUrl", config.getReturnUrl());
        // Use the actual peer IP. Forwarded headers require a trusted reverse proxy configuration.
        parameters.put("vnp_IpAddr", ipAddress);
        parameters.put("vnp_CreateDate", formatDate(payment.getCreatedAt()));
        parameters.put("vnp_ExpireDate", formatDate(expiresAt));
        String query = canonicalQuery(parameters);
        return config.getPaymentUrl() + "?" + query + "&vnp_SecureHash=" + sign(query);
    }

    public Optional<Map<String, String>> verify(MultiValueMap<String, String> input) {
        requireConfiguration();
        var parameters = new TreeMap<String, String>();
        for (var entry : input.entrySet()) {
            if (!entry.getKey().startsWith("vnp_")) continue;
            // Reject ambiguous callback parameters before checking their signature.
            if (entry.getValue().size() != 1 || entry.getValue().getFirst() == null) return Optional.empty();
            parameters.put(entry.getKey(), entry.getValue().getFirst());
        }
        String supplied = parameters.remove("vnp_SecureHash");
        parameters.remove("vnp_SecureHashType");
        parameters.values().removeIf(String::isEmpty);
        if (supplied == null || !supplied.matches("[a-fA-F0-9]{128}")) return Optional.empty();
        byte[] expected = HexFormat.of().parseHex(sign(canonicalQuery(parameters)));
        if (!MessageDigest.isEqual(expected, HexFormat.of().parseHex(supplied))) return Optional.empty();
        if (!properties.getVnpay().getTmnCode().equals(parameters.get("vnp_TmnCode"))) return Optional.empty();
        return Optional.of(Map.copyOf(parameters));
    }

    public String amount(BigDecimal amount) {
        String value;
        try {
            value = amount.movePointRight(2).toBigIntegerExact().toString();
        } catch (ArithmeticException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment amount must have at most two decimal places");
        }
        if (amount.signum() <= 0 || !value.matches("[0-9]{1,12}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount is outside VNPay's supported range");
        }
        return value;
    }

    public LocalDateTime parsePayDate(String value) {
        return LocalDateTime.parse(value, DATE).atZone(VNPAY_ZONE).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    private String formatDate(LocalDateTime utc) {
        return DATE.format(utc.atOffset(ZoneOffset.UTC).atZoneSameInstant(VNPAY_ZONE));
    }

    private String canonicalQuery(Map<String, String> parameters) {
        return new TreeMap<>(parameters).entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(properties.getVnpay().getHashSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Cannot initialize VNPay signature", exception);
        }
    }

    private void requireConfiguration() {
        var config = properties.getVnpay();
        if (config.getTmnCode() == null || config.getTmnCode().isBlank()
                || config.getHashSecret() == null || config.getHashSecret().isBlank()
                || !validUrl(config.getPaymentUrl(), true) || !validUrl(config.getReturnUrl(), false)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "VNPay Sandbox is not configured");
        }
    }

    private boolean validUrl(String value, boolean httpsOnly) {
        try {
            URI uri = URI.create(value);
            return uri.getHost() != null && uri.getUserInfo() == null && uri.getFragment() == null
                    && uri.getRawQuery() == null && ("https".equals(uri.getScheme())
                    || (!httpsOnly && "http".equals(uri.getScheme())));
        } catch (IllegalArgumentException | NullPointerException exception) {
            return false;
        }
    }
}
