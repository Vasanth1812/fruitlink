package com.fruitlink.auth.repository;

import com.fruitlink.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
    
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.role.name = :roleName")
    java.util.List<User> findByRoleName(@org.springframework.data.repository.query.Param("roleName") String roleName);
}
