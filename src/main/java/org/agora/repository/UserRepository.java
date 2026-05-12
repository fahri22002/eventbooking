package org.agora.repository; // Sesuaikan dengan nama package Anda

import org.agora.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // Dibutuhkan untuk FR-02 (Login)
    Optional<User> findByEmail(String email);

    // Dibutuhkan untuk FR-01 (Validasi email unik saat registrasi)
    boolean existsByEmail(String email);
}