package com.qlkhachsan.model;

import java.time.Instant;

import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Tai khoan dang nhap - bang {@code users_by_username} (BO SUNG).
 *
 * <pre>
 * PRIMARY KEY (username)
 * </pre>
 */
@Table("users_by_username")
public class AppUser {

    @PrimaryKey("username")
    private String username;

    /** Mat khau da bam BCrypt - KHONG BAO GIO luu mat khau tho. */
    @Column("password_hash")
    private String passwordHash;

    @Column("full_name")
    private String fullName;

    @Column("email")
    private String email;

    @Column("role")
    private UserRole role = UserRole.STAFF;

    @Column("enabled")
    private Boolean enabled = Boolean.TRUE;

    @Column("created_at")
    private Instant createdAt = Instant.now();

    public AppUser() {
    }

    public AppUser(String username, String passwordHash, String fullName, String email, UserRole role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    @Transient
    public boolean isActive() {
        return Boolean.TRUE.equals(enabled);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "AppUser{username=" + username + ", role=" + role + ", enabled=" + enabled + "}";
    }
}