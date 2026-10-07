package com.eventhub.backend.repository;

import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findFirstByRoleAndStatusOrderByIdAsc(Role role, String status);

    @Query("select u from User u where lower(trim(u.email)) = :email")
    Optional<User> findByNormalizedEmail(String email);

    @Query(value = "SELECT * FROM users WHERE id = :id", nativeQuery = true)
    Optional<User> findUserByIdNative(@Param("id") Integer id);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(:email)) AND id != :id)", nativeQuery = true)
    boolean existsByEmailAndIdNotNative(@Param("email") String email, @Param("id") Integer id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE users SET full_name = :fullName, phone = :phone, email = :email, date_of_birth = :dateOfBirth, gender = :gender, updated_at = CURRENT_TIMESTAMP AT TIME ZONE 'UTC' WHERE id = :id", nativeQuery = true)
    int updateUserDetailNative(
            @Param("id") Integer id,
            @Param("fullName") String fullName,
            @Param("phone") String phone,
            @Param("email") String email,
            @Param("dateOfBirth") java.time.LocalDate dateOfBirth,
            @Param("gender") String gender
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE users SET password = :password, updated_at = CURRENT_TIMESTAMP AT TIME ZONE 'UTC' WHERE id = :id", nativeQuery = true)
    int updatePasswordNative(@Param("id") Integer id, @Param("password") String password);
}
