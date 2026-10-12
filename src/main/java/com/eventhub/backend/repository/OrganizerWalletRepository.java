package com.eventhub.backend.repository;

import com.eventhub.backend.entity.OrganizerWallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizerWalletRepository extends JpaRepository<OrganizerWallet, Integer> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select w from OrganizerWallet w where w.id = :id")
    java.util.Optional<OrganizerWallet> findForUpdate(Integer id);
}
