package com.qlkhachsan.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

/**
 * Khoa chinh cua bang {@code bookings_by_guest}.
 *
 * <pre>
 * PRIMARY KEY (guest_id, check_in_date, booking_id)
 * WITH CLUSTERING ORDER BY (check_in_date DESC, booking_id DESC)
 * </pre>
 */
@PrimaryKeyClass
public class BookingByGuestKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @PrimaryKeyColumn(name = "guest_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String guestId;

    @PrimaryKeyColumn(name = "check_in_date", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private LocalDate checkInDate;

    @PrimaryKeyColumn(name = "booking_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID bookingId;

    public BookingByGuestKey() {
    }

    public BookingByGuestKey(String guestId, LocalDate checkInDate, UUID bookingId) {
        this.guestId = guestId;
        this.checkInDate = checkInDate;
        this.bookingId = bookingId;
    }

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BookingByGuestKey other)) {
            return false;
        }
        return Objects.equals(guestId, other.guestId)
                && Objects.equals(checkInDate, other.checkInDate)
                && Objects.equals(bookingId, other.bookingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guestId, checkInDate, bookingId);
    }

    @Override
    public String toString() {
        return "BookingByGuestKey{guestId=" + guestId + ", checkInDate=" + checkInDate + ", bookingId=" + bookingId + "}";
    }
}