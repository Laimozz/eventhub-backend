package com.eventhub.backend.repository;

import com.eventhub.backend.entity.WithdrawalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Integer> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"organizerWallet", "organizerWallet.organizer"})
    @org.springframework.data.jpa.repository.Query("select r from WithdrawalRequest r where (:status is null or r.status = :status)")
    org.springframework.data.domain.Page<WithdrawalRequest> search(String status, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select r from WithdrawalRequest r where r.id = :id")
    java.util.Optional<WithdrawalRequest> findForUpdate(Integer id);
}
