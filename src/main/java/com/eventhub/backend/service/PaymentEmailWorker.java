package com.eventhub.backend.service;

import com.eventhub.backend.config.ApplicationProperties;
import com.eventhub.backend.repository.PaymentEmailRepository;
import com.eventhub.backend.repository.PaymentEmailRepository.PendingEmail;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.util.HtmlUtils;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.payment-email.enabled", havingValue = "true")
public class PaymentEmailWorker {
    private static final Locale VIETNAM = Locale.forLanguageTag("vi-VN");
    private static final DateTimeFormatter PAID_AT = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    private final PaymentEmailRepository emails;
    private final TransactionTemplate transaction;
    private final JavaMailSender sender;
    private final ApplicationProperties properties;

    @Scheduled(scheduler = "paymentEmailScheduler", fixedDelayString = "${app.payment-email.poll-delay:5s}",
            initialDelayString = "${app.payment-email.poll-delay:5s}")
    public void dispatch() {
        for (int i = 0; i < 10; i++) {
            // Keep only this mail row locked during delivery, never the booking or payment.
            if (!Boolean.TRUE.equals(transaction.execute(status -> dispatchNext()))) {
                return;
            }
        }
    }

    private boolean dispatchNext() {
        var due = emails.lockNextDue();
        if (due.isEmpty()) return false;

        PendingEmail email = due.get();
        try {
            send(email);
        } catch (MailException exception) {
            emails.recordFailure(email.bookingId());
            log.warn("Payment confirmation email for booking {} failed on attempt {} ({})",
                    email.bookingId(), email.attempts() + 1, exception.getClass().getSimpleName());
            return true;
        }
        emails.markSent(email.bookingId());
        return true;
    }

    private void send(PendingEmail email) {
        var config = properties.getPaymentEmail();
        String link = config.getFrontendBaseUrl().resolve("/customer/bookings/" + email.bookingId() + "/tickets")
                .toASCIIString();
        String amount = NumberFormat.getNumberInstance(VIETNAM).format(email.amount()) + " VND";
        String paidAt = PAID_AT.format(email.paidAt().toInstant(ZoneOffset.UTC)) + " (giờ Việt Nam)";
        String text = """
                EventHub đã xác nhận thanh toán và phát hành vé điện tử cho bạn.

                Mã đơn: #%d
                Sự kiện: %s
                Số tiền đã thanh toán: %s
                Mã giao dịch VNPay: %s
                Thời gian thanh toán: %s

                Xem vé điện tử: %s
                """.formatted(email.bookingId(), email.eventName(), amount, email.transactionId(), paidAt, link);
        String html = """
                <h1>Thanh toán thành công</h1>
                <p>EventHub đã xác nhận thanh toán và phát hành vé điện tử cho bạn.</p>
                <ul>
                  <li>Mã đơn: #%d</li>
                  <li>Sự kiện: %s</li>
                  <li>Số tiền đã thanh toán: %s</li>
                  <li>Mã giao dịch VNPay: %s</li>
                  <li>Thời gian thanh toán: %s</li>
                </ul>
                <p><a href="%s">Xem vé điện tử</a></p>
                """.formatted(email.bookingId(), escapeHtml(email.eventName()), amount,
                escapeHtml(email.transactionId()), paidAt, escapeHtml(link));

        sender.send(message -> {
            var helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED,
                    StandardCharsets.UTF_8.name());
            helper.setValidateAddresses(true);
            helper.setFrom(config.getFrom(), "EventHub");
            helper.setTo(email.recipient());
            helper.setSubject("EventHub | Thanh toán thành công cho đơn #" + email.bookingId());
            helper.setText(text, html);
        });
    }

    private static String escapeHtml(String value) {
        return HtmlUtils.htmlEscape(value, StandardCharsets.UTF_8.name());
    }
}
