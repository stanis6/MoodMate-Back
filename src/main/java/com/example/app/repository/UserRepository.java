package com.example.app.repository;

import com.example.app.models.User;
import com.example.app.models.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByClassroomIdAndRoleOrderByLastNameAsc(UUID classroomId, UserRole role);
}
