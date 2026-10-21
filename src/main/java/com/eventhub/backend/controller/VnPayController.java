package com.eventhub.backend.controller;

import com.eventhub.backend.dto.response.BookingResponses.PaymentReturn;
import com.eventhub.backend.service.BookingService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/vnpay")
@RequiredArgsConstructor
@Slf4j
public class VnPayController {
    private final BookingService bookings;

    @GetMapping("/ipn")
    public Map<String, String> ipn(@RequestParam MultiValueMap<String, String> parameters) {
        try {
            return bookings.processIpn(parameters);
        } catch (DataAccessException | TransactionException exception) {
            // Let VNPay retry; never acknowledge a rolled-back payment as successful.
            log.error("VNPay IPN persistence failed ({})", exception.getClass().getSimpleName());
            return Map.of("RspCode", "99", "Message", "Unable to persist payment; please retry");
        }
    }

    @GetMapping("/return")
    public PaymentReturn paymentReturn(@RequestParam MultiValueMap<String, String> parameters) {
        return bookings.paymentReturn(parameters);
    }
}
