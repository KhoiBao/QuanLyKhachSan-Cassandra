package com.qlkhachsan.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Lich su dat phong theo khach hang - bang {@code bookings_by_guest} (Q3).
 *
 * <pre>
 * PRIMARY KEY (guest_id, check_in_date, booking_id)
 * WITH CLUSTERING ORDER BY (check_in_date DESC, booking_id DESC)
 * </pre>
 */
@Table("bookings_by_guest")
public class BookingByGuest {

    @PrimaryKey
    private BookingByGuestKey key = new BookingByGuestKey();

    @Column("hotel_id")
    private String hotelId;

    @Column("hotel_name")
    private String hotelName;

    @Column("room_number")
    private Integer roomNumber;

    @Column("check_out_date")
    private LocalDate checkOutDate;

    @Column("total_amount")
    private BigDecimal totalAmount;

    @Column("status")
    private BookingStatus status = BookingStatus.PENDING;

    // ===== BO SUNG =====
    @Column("guest_name")
    private String guestName;

    @Column("room_type")
    private String roomType;

    @Column("nights")
    private Integer nights;

    @Column("notes")
    private String notes;

    @Column("created_at")
    private Instant createdAt = Instant.now();

    public BookingByGuest() {
    }

    /* ================== TIEN ICH ================== */

    @Transient
    public UUID getBookingId() {
        return key == null ? null : key.getBookingId();
    }

    @Transient
    public String getGuestId() {
        return key == null ? null : key.getGuestId();
    }

    @Transient
    public LocalDate getCheckInDate() {
        return key == null ? null : key.getCheckInDate();
    }

    @Transient
    public boolean isActive() {
        return status != null && status.isActive();
    }

    /* ================== GETTER / SETTER ================== */

    public BookingByGuestKey getKey() {
        return key;
    }

    public void setKey(BookingByGuestKey key) {
        this.key = key;
    }

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

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Integer roomNumber) {
        this.roomNumber = roomNumber;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Integer getNights() {
        return nights;
    }

    public void setNights(Integer nights) {
        this.nights = nights;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "BookingByGuest{key=" + key + ", hotelId=" + hotelId + ", status=" + status + "}";
    }
}