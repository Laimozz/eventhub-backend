package com.eventhub.backend.repository;

import com.eventhub.backend.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RefreshToken t where t.tokenHash = :hash")
    Optional<RefreshToken> findByHashForUpdate(String hash);

    @EntityGraph(attributePaths = "user")
    @Query("select t from RefreshToken t where t.id = :id")
    Optional<RefreshToken> findSessionById(Integer id);

    @Modifying
    @Query("update RefreshToken t set t.revoked = true where t.id = :id and t.revoked = false")
    int revokeById(Integer id);
}
