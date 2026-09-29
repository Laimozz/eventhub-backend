package com.eventhub.backend.repository;

import com.eventhub.backend.entity.WithdrawalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Integer> {
}
