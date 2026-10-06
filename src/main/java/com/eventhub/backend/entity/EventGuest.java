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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "event_guests")
public class EventGuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter(AccessLevel.NONE)
    @JdbcTypeCode(SqlTypes.LOCAL_DATE_TIME)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @JdbcTypeCode(SqlTypes.LOCAL_DATE_TIME)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "events_id", nullable = false)
    private Event event;

    @Column(name = "name", nullable = true, length = 50)
    private String name;

    @Column(name = "role", nullable = true, length = 50)
    private String role;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "image_url", nullable = true)
    private String imageUrl;

    @PrePersist
    private void initializeTimestamps() {
        createdAt = LocalDateTime.now(ZoneOffset.UTC);
        updatedAt = createdAt;
    }

    @PreUpdate
    private void updateTimestamp() {
        updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
