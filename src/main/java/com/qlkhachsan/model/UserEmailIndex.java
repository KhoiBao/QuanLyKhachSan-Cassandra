package com.qlkhachsan.model;

import java.time.Instant;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Bang tra cuu tai khoan theo email - {@code users_by_email} (BO SUNG).
 * Dung de chan trung email khi dang ky tai khoan.
 *
 * <pre>
 * PRIMARY KEY (email)
 * </pre>
 */
@Table("users_by_email")
public class UserEmailIndex {

    @PrimaryKey("email")
    private String email;

    @Column("username")
    private String username;

    @Column("full_name")
    private String fullName;

    @Column("role")
    private UserRole role = UserRole.STAFF;

    @Column("created_at")
    private Instant createdAt = Instant.now();

    public UserEmailIndex() {
    }

    public static UserEmailIndex of(AppUser user) {
        UserEmailIndex index = new UserEmailIndex();
        index.setEmail(user.getEmail());
        index.setUsername(user.getUsername());
        index.setFullName(user.getFullName());
        index.setRole(user.getRole());
        index.setCreatedAt(user.getCreatedAt() == null ? Instant.now() : user.getCreatedAt());
        return index;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}