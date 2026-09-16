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
 * Dat phong theo khach san / ngay - bang {@code bookings_by_hotel_date} (Q4).
 * Day la bang phuc vu nghiep vu le tan: xem phong nao da dat theo ngay.
 *
 * <pre>
 * PRIMARY KEY ((hotel_id), check_in_date, booking_id)
 * WITH CLUSTERING ORDER BY (check_in_date ASC, booking_id ASC)
 * </pre>
 */
@Table("bookings_by_hotel_date")
public class BookingByHotelDate {

    @PrimaryKey
    private BookingByHotelDateKey key = new BookingByHotelDateKey();

    @Column("guest_id")
    private String guestId;

    @Column("guest_name")
    private String guestName;

    @Column("room_number")
    private Integer roomNumber;

    @Column("check_out_date")
    private LocalDate checkOutDate;

    @Column("total_amount")
    private BigDecimal totalAmount;

    @Column("status")
    private BookingStatus status = BookingStatus.PENDING;

    // ===== BO SUNG =====
    @Column("room_type")
    private String roomType;

    @Column("nights")
    private Integer nights;

    @Column("notes")
    private String notes;

    @Column("created_at")
    private Instant createdAt = Instant.now();

    public BookingByHotelDate() {
    }

    /* ================== TIEN ICH ================== */

    @Transient
    public UUID getBookingId() {
        return key == null ? null : key.getBookingId();
    }

    @Transient
    public String getHotelId() {
        return key == null ? null : key.getHotelId();
    }

    @Transient
    public LocalDate getCheckInDate() {
        return key == null ? null : key.getCheckInDate();
    }

    /* ================== GETTER / SETTER ================== */

    public BookingByHotelDateKey getKey() {
        return key;
    }

    public void setKey(BookingByHotelDateKey key) {
        this.key = key;
    }

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
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
        return "BookingByHotelDate{key=" + key + ", guestId=" + guestId + ", status=" + status + "}";
    }
}