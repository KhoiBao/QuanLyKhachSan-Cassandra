package com.qlkhachsan.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

/**
 * Khoa chinh cua bang {@code bookings_by_hotel_date}.
 *
 * <pre>
 * PRIMARY KEY ((hotel_id), check_in_date, booking_id)
 * WITH CLUSTERING ORDER BY (check_in_date ASC, booking_id ASC)
 * </pre>
 */
@PrimaryKeyClass
public class BookingByHotelDateKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @PrimaryKeyColumn(name = "hotel_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String hotelId;

    @PrimaryKeyColumn(name = "check_in_date", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private LocalDate checkInDate;

    @PrimaryKeyColumn(name = "booking_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID bookingId;

    public BookingByHotelDateKey() {
    }

    public BookingByHotelDateKey(String hotelId, LocalDate checkInDate, UUID bookingId) {
        this.hotelId = hotelId;
        this.checkInDate = checkInDate;
        this.bookingId = bookingId;
    }

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
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
        if (!(o instanceof BookingByHotelDateKey other)) {
            return false;
        }
        return Objects.equals(hotelId, other.hotelId)
                && Objects.equals(checkInDate, other.checkInDate)
                && Objects.equals(bookingId, other.bookingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hotelId, checkInDate, bookingId);
    }

    @Override
    public String toString() {
        return "BookingByHotelDateKey{hotelId=" + hotelId + ", checkInDate=" + checkInDate + ", bookingId=" + bookingId + "}";
    }
}