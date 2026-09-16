package com.qlkhachsan.model;

import java.time.Instant;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Bang tra cuu khach hang theo so dien thoai - {@code guests_by_phone} (BO SUNG).
 *
 * <p>Cassandra khong cho phep truy van theo cot thuong (phone) neu khong co
 * partition key rieng, nen can bang denormalized nay de:
 * le tan nhap so dien thoai -> tim nhanh ho so khach hang.</p>
 *
 * <pre>
 * PRIMARY KEY (phone)
 * </pre>
 */
@Table("guests_by_phone")
public class GuestByPhone {

    @PrimaryKey("phone")
    private String phone;

    @Column("guest_id")
    private String guestId;

    @Column("full_name")
    private String fullName;

    @Column("email")
    private String email;

    @Column("created_at")
    private Instant createdAt = Instant.now();

    public GuestByPhone() {
    }

    public static GuestByPhone of(Guest guest) {
        GuestByPhone index = new GuestByPhone();
        index.setPhone(guest.getPhone());
        index.setGuestId(guest.getGuestId());
        index.setFullName(guest.getFullName());
        index.setEmail(guest.getEmail());
        index.setCreatedAt(guest.getCreatedAt() == null ? Instant.now() : guest.getCreatedAt());
        return index;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}