package com.eventhub.backend.service;

import com.eventhub.backend.config.ApplicationProperties;
import com.eventhub.backend.entity.Payment;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class VnPayRefundTest {
    private final String secret="refund-unit-test-secret-not-for-production";
    private VnPayService service() {
        var properties=new ApplicationProperties();
        properties.getVnpay().setTmnCode("TESTCODE"); properties.getVnpay().setHashSecret(secret);
        return new VnPayService(properties);
    }
    private RefundPaymentSnapshot payment() {
        var payment=new Payment(); payment.setTransactionId("merchant-reference"); payment.setAmount(new BigDecimal("100000")); return RefundPaymentSnapshot.from(payment);
    }
    private Map<String,String> signed(String command,String type,String status) throws Exception {
        var result=new LinkedHashMap<String,String>();
        result.put("vnp_ResponseId","response1"); result.put("vnp_Command",command); result.put("vnp_ResponseCode","00");
        result.put("vnp_Message","OK"); result.put("vnp_TmnCode","TESTCODE"); result.put("vnp_TxnRef","merchant-reference");
        result.put("vnp_Amount","10000000"); result.put("vnp_BankCode","TESTBANK"); result.put("vnp_PayDate","20261022120000");
        result.put("vnp_TransactionNo","1234"); result.put("vnp_TransactionType",type); result.put("vnp_TransactionStatus",status);
        result.put("vnp_OrderInfo","refund test");
        String plain=String.join("|",result.values())+(command.equals("querydr")?"||":"");
        Mac mac=Mac.getInstance("HmacSHA512"); mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA512"));
        result.put("vnp_SecureHash",HexFormat.of().formatHex(mac.doFinal(plain.getBytes(StandardCharsets.UTF_8)))); return result;
    }
    @Test void confirmsOnlySignedCompletedFullRefund() throws Exception {
        assertThat(service().verifyApiResponse(signed("refund","02","00"),Map.of("vnp_Command","refund"),payment(),false).completed()).isTrue();
        for(String pending:List.of("01","05","06"))
            assertThat(service().verifyApiResponse(signed("refund","02",pending),Map.of("vnp_Command","refund"),payment(),false).completed()).isFalse();
    }
    @Test void successfulOriginalPaymentQueryCannotCompleteRefund() throws Exception {
        assertThat(service().verifyApiResponse(signed("querydr","01","00"),Map.of("vnp_Command","querydr"),payment(),true).completed()).isFalse();
        assertThat(service().verifyApiResponse(signed("querydr","02","00"),Map.of("vnp_Command","querydr"),payment(),true).completed()).isTrue();
    }
    @Test void rejectsTamperingAndWrongCommand() throws Exception {
        var response=signed("refund","02","00"); response.put("vnp_Amount","20000000");
        assertThatThrownBy(() -> service().verifyApiResponse(response,Map.of("vnp_Command","refund"),payment(),false)).isInstanceOf(RefundProviderFailure.VerificationException.class);
        var valid=signed("refund","02","00");
        assertThatThrownBy(() -> service().verifyApiResponse(valid,Map.of("vnp_Command","querydr"),payment(),false)).isInstanceOf(RefundProviderFailure.VerificationException.class);
    }

    private void resign(Map<String,String> response) throws Exception {
        var keys = new ArrayList<>(List.of("vnp_ResponseId","vnp_Command","vnp_ResponseCode","vnp_Message",
                "vnp_TmnCode","vnp_TxnRef","vnp_Amount","vnp_BankCode","vnp_PayDate","vnp_TransactionNo",
                "vnp_TransactionType","vnp_TransactionStatus","vnp_OrderInfo"));
        if ("querydr".equals(response.get("vnp_Command"))) keys.addAll(List.of("vnp_PromotionCode","vnp_PromotionAmount"));
        String plain=keys.stream().map(k -> Objects.toString(response.get(k),"")).collect(java.util.stream.Collectors.joining("|"));
        Mac mac=Mac.getInstance("HmacSHA512"); mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA512"));
        response.put("vnp_SecureHash",HexFormat.of().formatHex(mac.doFinal(plain.getBytes(StandardCharsets.UTF_8))));
    }

    private RefundProviderFailure failure(Map<String,String> response) {
        var error=catchThrowable(() -> service().verifyApiResponse(response,Map.of("vnp_Command","refund"),payment(),false));
        assertThat(error).isInstanceOf(RefundProviderFailure.VerificationException.class);
        return RefundProviderFailure.classify((RuntimeException)error);
    }

    @Test void unsignedErrorKeepsOnlyUnverifiedNumericHint() {
        var result=new HashMap<String,String>();
        result.put("vnp_ResponseCode","97"); result.put("vnp_Message","PRIVATE_PROVIDER_TEXT");
        var diagnostic=failure(result);
        assertThat(diagnostic.code()).isEqualTo("PROVIDER_SIGNATURE_MISSING");
        assertThat(diagnostic.message()).contains("97","chưa xác minh").doesNotContain("PRIVATE",secret);
        result.put("vnp_ResponseCode","PRIVATE_CODE");
        assertThat(failure(result).message()).doesNotContain("PRIVATE");
        result.put("vnp_SecureHash",null);
        assertThat(failure(result).code()).isEqualTo("PROVIDER_SIGNATURE_MISSING");
    }

    @Test void distinguishesInvalidSignatureFormatAndMismatch() throws Exception {
        var result=signed("refund","02","00");
        result.put("vnp_SecureHash","not-a-signature");
        assertThat(failure(result).code()).isEqualTo("PROVIDER_SIGNATURE_INVALID_FORMAT");
        result.put("vnp_SecureHash","0".repeat(128));
        assertThat(failure(result).code()).isEqualTo("PROVIDER_SIGNATURE_MISMATCH");
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({
        "vnp_TmnCode,OTHER,PROVIDER_MERCHANT_MISMATCH",
        "vnp_Command,other,PROVIDER_COMMAND_MISMATCH",
        "vnp_TxnRef,other-reference,PROVIDER_REFERENCE_MISMATCH",
        "vnp_Amount,20000000,PROVIDER_AMOUNT_MISMATCH"})
    void identifiesMismatchOnlyAfterVerifyingSignature(String field,String value,String code) throws Exception {
        var result=signed("refund","02","00"); result.put(field,value);
        assertThat(failure(result).code()).isEqualTo("PROVIDER_SIGNATURE_MISMATCH");
        resign(result);
        assertThat(failure(result).code()).isEqualTo(code);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings={"vnp_TmnCode","vnp_Command","vnp_TxnRef","vnp_Amount",
            "vnp_ResponseCode","vnp_TransactionType","vnp_TransactionStatus","vnp_TransactionNo"})
    void identifiesMissingFieldWithoutNullPointer(String field) throws Exception {
        var result=signed("refund","02","00"); result.put(field,null); resign(result);
        var diagnostic=failure(result);
        assertThat(diagnostic.code()).isEqualTo("PROVIDER_FIELD_MISSING");
        assertThat(diagnostic.message()).contains(field);
    }

    @Test void optionalNullFieldsAndUppercaseSignatureRemainValid() throws Exception {
        var result=signed("querydr","02","00"); result.put("vnp_PromotionCode",null); result.put("vnp_PromotionAmount",null);
        resign(result); result.put("vnp_SecureHash",result.get("vnp_SecureHash").toUpperCase(Locale.ROOT));
        assertThat(service().verifyApiResponse(result,Map.of("vnp_Command","querydr"),payment(),true).completed()).isTrue();
    }
}
