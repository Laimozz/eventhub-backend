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
    private Payment payment() {
        var payment=new Payment(); payment.setTransactionId("merchant-reference"); payment.setAmount(new BigDecimal("100000")); return payment;
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
        assertThatThrownBy(() -> service().verifyApiResponse(response,Map.of("vnp_Command","refund"),payment(),false)).isInstanceOf(IllegalStateException.class);
        var valid=signed("refund","02","00");
        assertThatThrownBy(() -> service().verifyApiResponse(valid,Map.of("vnp_Command","querydr"),payment(),false)).isInstanceOf(IllegalStateException.class);
    }
}
