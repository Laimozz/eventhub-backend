package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Payment;
import java.util.List;
import java.util.Optional;
import java.util.Collection;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
<<<<<<< HEAD
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"booking", "booking.user"})
    org.springframework.data.domain.Page<Payment> findByBookingEventId(Integer eventId, org.springframework.data.domain.Pageable pageable);
=======
    List<Payment> findByBookingIdOrderByIdDesc(Integer bookingId);
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findFirstByBookingIdAndStatusOrderByIdDesc(Integer bookingId, String status);
    boolean existsByPaymentMethodAndGatewayTransactionId(String paymentMethod, String gatewayTransactionId);

    @Query("select distinct p.booking.id from Payment p where p.booking.id in :bookingIds and p.paymentMethod = 'VNPAY' and p.status = 'PENDING'")
    Set<Integer> findPendingVnpayBookingIds(Collection<Integer> bookingIds);

    @Query("select p.booking.id as bookingId, p.booking.event.id as eventId from Payment p where p.transactionId = :reference")
    Optional<Reference> findReference(String reference);

    interface Reference {
        Integer getBookingId();
        Integer getEventId();
    }
>>>>>>> develop
}
