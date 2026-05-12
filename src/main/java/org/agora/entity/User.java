package org.agora.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "\"User\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails { // <-- Tambahkan implements UserDetails

    @Id
    @Column(name = "\"userId\"", nullable = false)
    private String userId;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "password")
    private String password;

    @Column(name = "\"createAt\"")
    private ZonedDateTime createAt;

    // ==========================================================
    // METHOD WAJIB DARI SPRING SECURITY (USERDETAILS)
    // ==========================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Kita kosongkan karena tidak ada pembagian role Admin/User
        return List.of();
    }

    @Override
    public String getUsername() {
        // Penting: Spring Security menggunakan method getUsername() untuk mengidentifikasi user.
        // Karena sistem kita menggunakan email untuk login, kita return email di sini.
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}