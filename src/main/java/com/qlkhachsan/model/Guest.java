package com.qlkhachsan.model;

import java.time.Instant;

import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Ho so khach hang - bang {@code guests} (BO SUNG).
 *
 * <pre>
 * PRIMARY KEY (guest_id)
 * </pre>
 */
@Table("guests")
public class Guest {

    @PrimaryKey("guest_id")
    private String guestId;

    @Column("full_name")
    private String fullName;

    @Column("phone")
    private String phone;

    @Column("email")
    private String email;

    @Column("cccd")
    private String cccd;

    @Column("address")
    private String address;

    @Column("created_at")
    private Instant createdAt = Instant.now();

    public Guest() {
    }

    public Guest(String guestId, String fullName, String phone) {
        this.guestId = guestId;
        this.fullName = fullName;
        this.phone = phone;
    }

    /** Ten hien thi day du: "KH001 - Nguyen Van An". */
    @Transient
    public String getDisplayName() {
        return (guestId == null ? "" : guestId) + " - " + (fullName == null ? "" : fullName);
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Guest{guestId=" + guestId + ", fullName=" + fullName + ", phone=" + phone + "}";
    }
}