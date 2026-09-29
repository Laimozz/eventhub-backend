package com.eventhub.backend.repository;

import com.eventhub.backend.entity.PayoutTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayoutTransactionRepository extends JpaRepository<PayoutTransaction, Integer> {
}
