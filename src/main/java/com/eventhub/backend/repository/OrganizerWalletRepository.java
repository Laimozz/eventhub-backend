package com.eventhub.backend.repository;

import com.eventhub.backend.entity.OrganizerWallet;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizerWalletRepository extends JpaRepository<OrganizerWallet, Integer> {
    Optional<OrganizerWallet> findByOrganizerId(Integer organizerId);
}
