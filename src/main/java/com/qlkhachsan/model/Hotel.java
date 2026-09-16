package com.qlkhachsan.model;

import java.time.Instant;

import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Khach san - bang {@code hotels} (Q1).
 *
 * <pre>
 * PRIMARY KEY (hotel_id)
 * </pre>
 */
@Table("hotels")
public class Hotel {

    @PrimaryKey("hotel_id")
    private String hotelId;

    @Column("hotel_name")
    private String hotelName;

    @Column("city")
    private String city;

    @Column("address")
    private String address;

    @Column("star_rating")
    private Integer starRating;

    @Column("phone")
    private String phone;

    // ===== BO SUNG =====
    @Column("email")
    private String email;

    @Column("status")
    private HotelStatus status = HotelStatus.ACTIVE;

    @Column("total_rooms")
    private Integer totalRooms = 0;

    @Column("created_at")
    private Instant createdAt = Instant.now();

    public Hotel() {
    }

    public Hotel(String hotelId, String hotelName) {
        this.hotelId = hotelId;
        this.hotelName = hotelName;
    }

    /* ================== TIEN ICH CHO VIEW / NGHIEP VU ================== */

    /** Ten day du dung khi hien thi (ma + ten). */
    @Transient
    public String getDisplayName() {
        return (hotelId == null ? "" : hotelId) + " - " + (hotelName == null ? "" : hotelName);
    }

    @Transient
    public boolean isActive() {
        return status == HotelStatus.ACTIVE;
    }

    /** So sao dang text de hien thi nhanh. */
    @Transient
    public String getStarText() {
        int stars = starRating == null ? 0 : Math.max(0, Math.min(5, starRating));
        return "★".repeat(stars) + "☆".repeat(5 - stars);
    }

    /* ================== GETTER / SETTER ================== */

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getStarRating() {
        return starRating;
    }

    public void setStarRating(Integer starRating) {
        this.starRating = starRating;
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

    public HotelStatus getStatus() {
        return status;
    }

    public void setStatus(HotelStatus status) {
        this.status = status;
    }

    public Integer getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(Integer totalRooms) {
        this.totalRooms = totalRooms;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Hotel{hotelId=" + hotelId + ", hotelName=" + hotelName + ", city=" + city + "}";
    }
}