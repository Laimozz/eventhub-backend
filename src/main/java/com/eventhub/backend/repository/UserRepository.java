package com.eventhub.backend.repository;

import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("""
            select u from User u where (:role is null or u.role = :role)
            and (lower(coalesce(u.fullName, '')) like :search escape '!'
            or lower(u.email) like :search escape '!')
            """)
    org.springframework.data.domain.Page<User> searchAdminUsers(Role role, String search,
            org.springframework.data.domain.Pageable pageable);
    Optional<User> findFirstByRoleAndStatusOrderByIdAsc(Role role, String status);

    @Query("select u from User u where lower(trim(u.email)) = :email")
    Optional<User> findByNormalizedEmail(String email);
}
