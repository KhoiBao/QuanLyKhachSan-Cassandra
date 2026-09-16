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
 * Bang tra cuu don dat phong theo {@code booking_id} - {@code bookings_by_id}
 * (BO SUNG).
 *
 * <p>Ly do: o hai bang {@code bookings_by_guest} va {@code bookings_by_hotel_date},
 * {@code booking_id} chi la CLUSTERING key nen khong the {@code SELECT ... WHERE booking_id = ?}
 * neu khong dung ALLOW FILTERING. Bang nay la ban sao phang giup xem chi tiet /
 * sua / xoa don dat phong theo id mot cach hieu qua.</p>
 *
 * <pre>
 * PRIMARY KEY (booking_id)
 * </pre>
 */
@Table("bookings_by_id")
public class BookingById {

    @PrimaryKey("booking_id")
    private UUID bookingId;

    @Column("guest_id")
    private String guestId;

    @Column("guest_name")
    private String guestName;

    @Column("hotel_id")
    private String hotelId;

    @Column("hotel_name")
    private String hotelName;

    @Column("room_number")
    private Integer roomNumber;

    @Column("room_type")
    private String roomType;

    @Column("check_in_date")
    private LocalDate checkInDate;

    @Column("check_out_date")
    private LocalDate checkOutDate;

    @Column("nights")
    private Integer nights;

    @Column("total_amount")
    private BigDecimal totalAmount;

    @Column("status")
    private BookingStatus status = BookingStatus.PENDING;

    @Column("notes")
    private String notes;

    @Column("created_at")
    private Instant createdAt = Instant.now();

    public BookingById() {
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
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

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public Integer getNights() {
        return nights;
    }

    public void setNights(Integer nights) {
        this.nights = nights;
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

    /** Ma don hien thi tren giao dien, vi du "DP-3F2A1B" (8 ky tu dau cua UUID). */
    @Transient
    public String getShortCode() {
        return bookingId == null ? "" : "DP-" + bookingId.toString().substring(0, 8).toUpperCase();
    }

    @Override
    public String toString() {
        return "BookingById{bookingId=" + bookingId + ", guestId=" + guestId + ", status=" + status + "}";
    }
}