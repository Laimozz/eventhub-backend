package com.eventhub.backend.repository;

import com.eventhub.backend.entity.AiConversation;
import com.eventhub.backend.enums.Role;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface AiConversationRepository extends JpaRepository<AiConversation, Integer> {
    List<AiConversation> findTop50ByUser_IdAndOwnerRoleOrderByUpdatedAtDescIdDesc(Integer userId, Role ownerRole);

    Optional<AiConversation> findByIdAndUser_IdAndOwnerRole(Integer id, Integer userId, Role ownerRole);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from AiConversation c where c.id = :id and c.user.id = :userId and c.ownerRole = :role")
    Optional<AiConversation> lockOwned(Integer id, Integer userId, Role role);
}
