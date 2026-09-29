package com.eventhub.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "refunds", uniqueConstraints = {
        @UniqueConstraint(name = "uk_refunds_refund_transaction_id", columnNames = {"refund_transaction_id"})
})
public class Refund extends AuditedEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payments_id", nullable = false, unique = true)
    private Payment payment;

    @Column(name = "refund_transaction_id", nullable = false)
    private String refundTransactionId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "last_error", nullable = true)
    private String lastError;

    @Column(name = "refunded_at", nullable = true)
    private LocalDateTime refundedAt;
}
