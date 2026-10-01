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
@Table(name = "tickets", uniqueConstraints = {
        @UniqueConstraint(name = "uk_tickets_ticket_code", columnNames = {"ticket_code"}),
        @UniqueConstraint(name = "uk_tickets_qr_token", columnNames = {"qr_token"})
})
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_items_id", nullable = false)
    private BookingItem bookingItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "checked_in_by", nullable = true)
    private User checkedInBy;

    @Column(name = "ticket_code", nullable = false)
    private String ticketCode;

    @Column(name = "qr_token", nullable = false)
    private String qrToken;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "checked_in_at", nullable = true)
    private LocalDateTime checkedInAt;

    @PrePersist
    private void initializeCreatedAt() {
        createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
