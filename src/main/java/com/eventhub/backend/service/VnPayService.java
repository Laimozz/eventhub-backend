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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class VnPayService {
    private static final ZoneId VNPAY_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("uuuuMMddHHmmss")
            .withResolverStyle(ResolverStyle.STRICT);
    private final ApplicationProperties properties;

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
