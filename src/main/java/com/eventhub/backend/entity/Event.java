package com.eventhub.backend.entity;

import com.eventhub.backend.enums.EventStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "events")
public class Event {

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
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "reviewed_by", nullable = true)
    private User reviewedBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venues_id", nullable = false)
    private Venue venue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categories_id", nullable = false)
    private Category category;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "thumbnail_image_url", nullable = false)
    private String thumbnailImageUrl;

    @Column(name = "banner_image_url", nullable = false)
    private String bannerImageUrl;

    @Column(name = "image_zone_url", nullable = true)
    private String imageZoneUrl;

    // Bind UTC wall-clock values directly, without the JVM default time zone shifting them.
    @JdbcTypeCode(SqlTypes.LOCAL_DATE_TIME)
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @JdbcTypeCode(SqlTypes.LOCAL_DATE_TIME)
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventStatus status;

    @Column(name = "cancel_reason", nullable = true)
    private String cancelReason;

    @JdbcTypeCode(SqlTypes.LOCAL_DATE_TIME)
    @Column(name = "canceled_at", nullable = true)
    private LocalDateTime canceledAt;

    @Column(name = "reject_reason", nullable = true)
    private String rejectReason;

    @JdbcTypeCode(SqlTypes.LOCAL_DATE_TIME)
    @Column(name = "reviewed_at", nullable = true)
    private LocalDateTime reviewedAt;

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
