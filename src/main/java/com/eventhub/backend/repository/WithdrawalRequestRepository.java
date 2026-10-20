package com.eventhub.backend.repository;

import com.eventhub.backend.entity.WithdrawalRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Integer> {

    @Query("SELECT r FROM WithdrawalRequest r WHERE r.organizerWallet.organizer.id = :organizerId ORDER BY r.requestedAt DESC")
    List<WithdrawalRequest> findByOrganizerIdOrderByRequestedAtDesc(@Param("organizerId") Integer organizerId);

    @Query("""
        SELECT r FROM WithdrawalRequest r
        WHERE r.organizerWallet.organizer.id = :organizerId
          AND (:status IS NULL OR UPPER(r.status) = UPPER(:status))
        ORDER BY r.requestedAt DESC
    """)
    List<WithdrawalRequest> findByOrganizerIdAndStatus(
            @Param("organizerId") Integer organizerId,
            @Param("status") String status
    );

    @Query("""
        SELECT r FROM WithdrawalRequest r
        WHERE r.id = :id AND r.organizerWallet.organizer.id = :organizerId
    """)
    Optional<WithdrawalRequest> findByIdAndOrganizerId(
            @Param("id") Integer id,
            @Param("organizerId") Integer organizerId
    );
}
