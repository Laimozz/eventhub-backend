package com.eventhub.backend.service;

import com.eventhub.backend.config.ApplicationProperties;
import com.eventhub.backend.dto.request.CreateBookingRequest;
import com.eventhub.backend.dto.response.BookingResponses.*;
import com.eventhub.backend.entity.*;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.repository.*;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookings;
    private final BookingItemRepository bookingItems;
    private final TicketTypeRepository ticketTypes;
    private final TicketRepository tickets;
    private final PaymentRepository payments;
    private final EventRepository events;
    private final UserRepository users;
    private final VnPayService vnpay;
    private final ApplicationProperties properties;
    private final Clock clock;
    private final NotificationService notifications;
<<<<<<< HEAD
    private final FinanceLedgerService financeLedger;
=======
    private final AiOutboxService aiOutbox;
>>>>>>> develop

    @Transactional(readOnly = true)
    public List<TicketTypeInfo> listTicketTypes(Integer userId, Integer eventId) {
        requireCustomer(userId);
        Event event = events.findById(eventId).orElseThrow(() -> notFound("Event"));
        if (event.getStatus() != EventStatus.APPROVED) throw notFound("Bookable event");
        var now = LocalDateTime.now(clock);
        return ticketTypes.findByEventIdOrderByIdAsc(eventId).stream()
                .filter(type -> "ACTIVE".equals(type.getStatus()))
                .map(type -> new TicketTypeInfo(type.getId(), type.getName(), type.getDescription(), type.getImageUrl(),
                        type.getPrice(), "VND", type.getRemainingQuantity(), type.getSaleStartTime(), type.getSaleEndTime(),
                        event.getStartTime().isAfter(now) && isOnSale(type, now) && type.getRemainingQuantity() > 0))
                .toList();
    }

    @Transactional
    public Detail create(Integer userId, CreateBookingRequest request) {
        User customer = requireCustomer(userId);
        // Lock the event first, as Organizer/Admin do, to serialize inventory changes and event lifecycle changes.
        Event event = lockEvent(request.eventId());
        var now = LocalDateTime.now(clock);
        requireBookable(event, now);
        var types = ticketTypes.findForUpdate(event.getId());
        releaseExpired(event.getId(), now, types);
        var byId = types.stream().collect(Collectors.toMap(TicketType::getId, Function.identity()));
        if (request.items().stream().map(CreateBookingRequest.Item::ticketTypeId).distinct().count() != request.items().size()
                || request.items().stream().mapToInt(CreateBookingRequest.Item::quantity).sum() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate ticket type or more than 100 tickets per booking");
        }
        BigDecimal total = BigDecimal.ZERO;
        LocalDateTime expiresAt = now.plus(properties.getBooking().getHoldTtl());
        for (var item : request.items()) {
            var type = byId.get(item.ticketTypeId());
            if (type == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket type does not belong to this event");
            if (!isOnSale(type, now)) throw conflict("Ticket type is not on sale");
            if (type.getRemainingQuantity() < item.quantity()) throw conflict("Not enough tickets available");
            total = total.add(type.getPrice().multiply(BigDecimal.valueOf(item.quantity())));
            // A hold cannot outlive the ticket sale window or the event start.
            if (type.getSaleEndTime().isBefore(expiresAt)) expiresAt = type.getSaleEndTime();
        }
        if (event.getStartTime().isBefore(expiresAt)) expiresAt = event.getStartTime();
        if (total.precision() - total.scale() > 17) throw conflict("Booking total exceeds the supported amount");
        Booking booking = new Booking();
        booking.setUser(customer);
        booking.setEvent(event);
        booking.setStatus("PENDING");
        booking.setTotalAmount(total);
        booking.setExpiredAt(expiresAt);
        bookings.save(booking);
        var items = new ArrayList<BookingItem>();
        for (var input : request.items()) {
            var type = byId.get(input.ticketTypeId());
            type.setRemainingQuantity(type.getRemainingQuantity() - input.quantity());
            type.setReservedQuantity(type.getReservedQuantity() + input.quantity());
            BookingItem item = new BookingItem();
            item.setBooking(booking);
            item.setTicketType(type);
            item.setQuantity(input.quantity());
            item.setUnitPrice(type.getPrice()); // Price snapshot: never accept an amount from the client.
            items.add(item);
        }
        bookingItems.saveAll(items);
        if (total.signum() == 0) {
            Payment payment = newPayment(booking, "FREE");
            payment.setStatus("SUCCESS");
            payment.setPaidAt(now);
            payments.save(payment);
            issueTickets(booking, items);
            payments.flush();
            financeLedger.recordPayment(payment);
        }
        bookings.flush();
        return detail(booking);
    }

    @Transactional(readOnly = true)
    public PageResponse history(Integer userId, int page, int size) {
        requireCustomer(userId);
        if (page < 0 || size < 1 || size > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be nonnegative and size between 1 and 50");
        }
        var result = bookings.findByUserId(userId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id")));
        var awaiting = result.isEmpty() ? Set.<Integer>of()
                : payments.findPendingVnpayBookingIds(result.map(Booking::getId).toList());
        var now = LocalDateTime.now(clock);
        return new PageResponse(result.stream().map(booking -> {
            var deadline = confirmationDeadline(booking, awaiting.contains(booking.getId()));
            return new Summary(booking.getId(), visibleStatus(booking, deadline, now),
                    booking.getTotalAmount(), "VND", booking.getCreatedAt(), booking.getExpiredAt(), deadline, eventInfo(booking.getEvent()));
        })
                .toList(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public Detail get(Integer userId, Integer bookingId) {
        requireCustomer(userId);
        return detail(bookings.findByIdAndUserId(bookingId, userId).orElseThrow(() -> notFound("Booking")));
    }

    @Transactional
    public PaymentUrl startPayment(Integer userId, Integer bookingId, String ipAddress) {
        requireCustomer(userId);
        Integer eventId = bookings.findOwnedEventId(bookingId, userId).orElseThrow(() -> notFound("Booking"));
        Event event = lockEvent(eventId);
        Booking booking = bookings.findByIdAndUserId(bookingId, userId).orElseThrow(() -> notFound("Booking"));
        var now = LocalDateTime.now(clock);
        if (!"PENDING".equals(booking.getStatus()) || !booking.getExpiredAt().isAfter(now)) {
            throw conflict("Booking is already paid, expired or cannot be paid");
        }
        requireBookable(event, now);
        Payment payment = payments.findFirstByBookingIdAndStatusOrderByIdDesc(bookingId, "PENDING")
                .orElseGet(() -> payments.saveAndFlush(newPayment(booking, "VNPAY")));
        String url = vnpay.createPaymentUrl(payment, ipAddress, booking.getExpiredAt());
        return new PaymentUrl(bookingId, payment.getId(), payment.getTransactionId(), url, booking.getExpiredAt());
    }

    @Transactional
    public Map<String, String> processIpn(MultiValueMap<String, String> input) {
        var verified = vnpay.verify(input);
        if (verified.isEmpty()) return ipn("97", "Invalid signature");
        var data = verified.get();
        if (!validResult(data)) return ipn("99", "Invalid callback parameters");
        var reference = payments.findReference(data.get("vnp_TxnRef"));
        if (reference.isEmpty()) return ipn("01", "Order not found");
        Event event = lockEvent(reference.get().getEventId());
        // Load mutable payment/booking state only after acquiring the event lock.
        Payment payment = payments.findByTransactionId(data.get("vnp_TxnRef")).orElseThrow();
        Booking booking = bookings.findById(reference.get().getBookingId()).orElseThrow();
        if (!"VNPAY".equals(payment.getPaymentMethod())) return ipn("01", "Order not found");
        if (!vnpay.amount(payment.getAmount()).equals(data.get("vnp_Amount"))) return ipn("04", "Invalid amount");
        if ("SUCCESS".equals(payment.getStatus()) || "FAILED".equals(payment.getStatus())) {
            return ipn("02", "Order already confirmed");
        }
        boolean success = "00".equals(data.get("vnp_ResponseCode")) && "00".equals(data.get("vnp_TransactionStatus"));
        LocalDateTime paidAt = null;
        if (success) {
            String transactionNo = data.getOrDefault("vnp_TransactionNo", "");
            if (!transactionNo.matches("[0-9]{1,15}") || Long.parseLong(transactionNo) == 0) {
                return ipn("99", "Invalid gateway transaction");
            }
            if (payments.existsByPaymentMethodAndGatewayTransactionId("VNPAY", transactionNo)) {
                return ipn("99", "Gateway transaction already used");
            }
            try {
                paidAt = vnpay.parsePayDate(data.getOrDefault("vnp_PayDate", ""));
            } catch (DateTimeParseException exception) {
                return ipn("99", "Invalid payment date");
            }
            if (paidAt.isBefore(payment.getCreatedAt().withNano(0))
                    || paidAt.isAfter(LocalDateTime.now(clock).plusSeconds(60))) {
                return ipn("99", "Invalid payment date");
            }
            payment.setGatewayTransactionId(transactionNo);
            payment.setPaidAt(paidAt);
        }
        payment.setResponseCode(data.get("vnp_ResponseCode"));
        payment.setStatus(success ? "SUCCESS" : "FAILED");
        var types = ticketTypes.findForUpdate(event.getId());
        if (success) {
            boolean held = "PENDING".equals(booking.getStatus());
            // Enforce the grace deadline even if the expiry job has not run yet.
            if (held && LocalDateTime.now(clock).isBefore(booking.getExpiredAt().plus(properties.getBooking().getIpnGracePeriod()))
                    && !paidAt.isAfter(booking.getExpiredAt()) && event.getStatus() == EventStatus.APPROVED) {
                issueTickets(booking, bookingItems.findByBookingIdOrderByIdAsc(booking.getId()));
            } else {
                // Record actual money received even if a late IPN arrives after seats were released.
                // Never issue seats that may now belong to another customer.
                if (held) releaseReservation(booking, types);
                booking.setStatus("PAYMENT_REVIEW");
            }
        } else if ("PENDING".equals(booking.getStatus()) && !booking.getExpiredAt().isAfter(LocalDateTime.now(clock))) {
            releaseReservation(booking, types);
            booking.setStatus("EXPIRED");
        }
        payments.flush(); // DB errors must reach the controller before it acknowledges the IPN.
        if (success) financeLedger.recordPayment(payment);
        return ipn("00", "Confirm Success");
    }

    @Transactional(readOnly = true)
    public PaymentReturn paymentReturn(MultiValueMap<String, String> input) {
        var data = vnpay.verify(input).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid VNPay signature"));
        if (!validResult(data)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid callback parameters");
        Payment payment = payments.findByTransactionId(data.get("vnp_TxnRef")).orElseThrow(() -> notFound("Payment"));
        if (!"VNPAY".equals(payment.getPaymentMethod()) || !vnpay.amount(payment.getAmount()).equals(data.get("vnp_Amount"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment amount or method");
        }
        // A browser redirect only displays the verified result. Only IPN can confirm payment.
        Booking booking = payment.getBooking();
        var deadline = confirmationDeadline(booking, !payments.findPendingVnpayBookingIds(List.of(booking.getId())).isEmpty());
        return new PaymentReturn(booking.getId(), visibleStatus(booking, deadline, LocalDateTime.now(clock)), payment.getStatus(),
                data.get("vnp_ResponseCode"), data.get("vnp_TransactionStatus"));
    }

    @Transactional
    public void expireBookings(Integer eventId) {
        lockEvent(eventId);
        releaseExpired(eventId, LocalDateTime.now(clock), ticketTypes.findForUpdate(eventId));
    }

    private void releaseExpired(Integer eventId, LocalDateTime now, List<TicketType> types) {
        for (Booking booking : bookings.findReleasableBookings(
                eventId, now, now.minus(properties.getBooking().getIpnGracePeriod()), PageRequest.of(0, 500))) {
            releaseReservation(booking, types);
            booking.setStatus("EXPIRED");
            payments.findByBookingIdOrderByIdDesc(booking.getId()).stream()
                    .filter(payment -> "PENDING".equals(payment.getStatus())).forEach(payment -> payment.setStatus("EXPIRED"));
        }
    }

    private void releaseReservation(Booking booking, List<TicketType> types) {
        var byId = types.stream().collect(Collectors.toMap(TicketType::getId, Function.identity()));
        for (var item : bookingItems.findByBookingIdOrderByIdAsc(booking.getId())) {
            var type = byId.get(item.getTicketType().getId());
            type.setReservedQuantity(type.getReservedQuantity() - item.getQuantity());
            type.setRemainingQuantity(type.getRemainingQuantity() + item.getQuantity());
        }
    }

    private void issueTickets(Booking booking, List<BookingItem> items) {
        var issued = new ArrayList<Ticket>();
        for (var item : items) {
            var type = item.getTicketType();
            type.setReservedQuantity(type.getReservedQuantity() - item.getQuantity());
            for (int index = 0; index < item.getQuantity(); index++) {
                Ticket ticket = new Ticket();
                ticket.setBookingItem(item);
                ticket.setTicketCode("EH" + UUID.randomUUID().toString().replace("-", ""));
                ticket.setQrToken(UUID.randomUUID().toString());
                ticket.setStatus("VALID");
                issued.add(ticket);
            }
        }
        tickets.saveAll(issued);
        booking.setStatus("PAID");
        notifications.create(booking.getUser(), "Đặt vé thành công",
                "Booking #" + booking.getId() + " cho sự kiện \"" + booking.getEvent().getName()
                        + "\" đã được xác nhận. Bạn có thể xem vé điện tử trên web.", "BOOKING_CONFIRMED");
        notifications.create(booking.getEvent().getOrganizer(), "Có booking được xác nhận",
                "Booking #" + booking.getId() + " cho sự kiện \"" + booking.getEvent().getName()
                        + "\" đã được xác nhận.", "BOOKING_CONFIRMED");
        aiOutbox.logBookingInteraction(booking.getUser().getId(), booking.getEvent().getId());
    }

    @Transactional
    public void cancelReservations(Integer eventId) {
        lockEvent(eventId);
        var types = ticketTypes.findForUpdate(eventId);
        for (var booking : bookings.findByEventIdAndStatus(eventId, "PENDING")) {
            releaseReservation(booking, types);
            booking.setStatus("EXPIRED");
        }
        bookings.flush();
    }

    private Detail detail(Booking booking) {
        var issued = tickets.findByBookingItemBookingIdOrderByIdAsc(booking.getId()).stream()
                .collect(Collectors.groupingBy(ticket -> ticket.getBookingItem().getId()));
        var items = bookingItems.findByBookingIdOrderByIdAsc(booking.getId()).stream().map(item -> new Item(item.getId(),
                item.getTicketType().getId(), item.getTicketType().getName(), item.getQuantity(), item.getUnitPrice(),
                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                issued.getOrDefault(item.getId(), List.of()).stream().map(ticket -> new TicketInfo(ticket.getId(),
                        ticket.getTicketCode(), "VALID".equals(ticket.getStatus()) ? ticket.getQrToken() : null,
                        ticket.getStatus(), ticket.getCheckedInAt())).toList())).toList();
        var paymentInfo = payments.findByBookingIdOrderByIdDesc(booking.getId()).stream().map(payment -> new PaymentInfo(
                payment.getId(), payment.getTransactionId(), payment.getGatewayTransactionId(), payment.getPaymentMethod(),
                payment.getAmount(), payment.getStatus(), payment.getResponseCode(), payment.getCreatedAt(), payment.getPaidAt())).toList();
        var deadline = confirmationDeadline(booking, paymentInfo.stream()
                .anyMatch(payment -> "VNPAY".equals(payment.paymentMethod()) && "PENDING".equals(payment.status())));
        return new Detail(booking.getId(), visibleStatus(booking, deadline, LocalDateTime.now(clock)), booking.getTotalAmount(), "VND", booking.getCreatedAt(),
                booking.getExpiredAt(), deadline, eventInfo(booking.getEvent()), items, paymentInfo);
    }

    private EventInfo eventInfo(Event event) {
        return new EventInfo(event.getId(), event.getName(), event.getThumbnailImageUrl(), event.getStatus().name(),
                event.getStartTime(), event.getEndTime(), event.getVenue().getCity(), event.getVenue().getAddress());
    }

    private LocalDateTime confirmationDeadline(Booking booking, boolean awaitingPayment) {
        return "PENDING".equals(booking.getStatus()) && awaitingPayment
                ? booking.getExpiredAt().plus(properties.getBooking().getIpnGracePeriod()) : null;
    }

    private String visibleStatus(Booking booking, LocalDateTime deadline, LocalDateTime now) {
        if (!"PENDING".equals(booking.getStatus()) || booking.getExpiredAt().isAfter(now)) return booking.getStatus();
        // Display-only state: PENDING in storage keeps the existing inventory reservation intact.
        return deadline != null && deadline.isAfter(now) ? "PAYMENT_CONFIRMING" : "EXPIRED";
    }

    private Payment newPayment(Booking booking, String method) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setTransactionId(UUID.randomUUID().toString().replace("-", ""));
        payment.setPaymentMethod(method);
        payment.setAmount(booking.getTotalAmount());
        payment.setStatus("PENDING");
        return payment;
    }

    private boolean validResult(Map<String, String> data) {
        return !data.getOrDefault("vnp_TxnRef", "").isBlank()
                && data.getOrDefault("vnp_Amount", "").matches("[0-9]{1,12}")
                && data.getOrDefault("vnp_ResponseCode", "").matches("[0-9]{2}")
                && data.getOrDefault("vnp_TransactionStatus", "").matches("[0-9]{2}");
    }

    private boolean isOnSale(TicketType type, LocalDateTime now) {
        return "ACTIVE".equals(type.getStatus()) && !now.isBefore(type.getSaleStartTime()) && now.isBefore(type.getSaleEndTime());
    }

    private void requireBookable(Event event, LocalDateTime now) {
        if (event.getStatus() != EventStatus.APPROVED || !event.getStartTime().isAfter(now)) {
            throw conflict("Event is not approved or has already started");
        }
    }

    private Event lockEvent(Integer id) {
        return events.findForReview(id).orElseThrow(() -> notFound("Event"));
    }

    private User requireCustomer(Integer id) {
        User user = users.findById(id).orElseThrow(() -> new AccessDeniedException("Customer account unavailable"));
        if (!user.isActive() || user.getRole() != Role.CUSTOMER) throw new AccessDeniedException("Only active customers can book tickets");
        return user;
    }

    private Map<String, String> ipn(String code, String message) {
        return Map.of("RspCode", code, "Message", message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private ResponseStatusException notFound(String resource) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, resource + " not found");
    }
}
