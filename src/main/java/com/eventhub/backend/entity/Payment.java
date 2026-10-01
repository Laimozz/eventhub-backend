package com.eventhub.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payments_transaction_id", columnNames = {"transaction_id"})
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bookings_id", nullable = false)
    private Booking booking;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "payment_method", nullable = true)
    private String paymentMethod;

    @Column(name = "amount", nullable = true, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "status", nullable = true)
    private String status;

    @Column(name = "paid_at", nullable = true)
    private LocalDateTime paidAt;

    @PrePersist
    private void initializeCreatedAt() {
        createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
