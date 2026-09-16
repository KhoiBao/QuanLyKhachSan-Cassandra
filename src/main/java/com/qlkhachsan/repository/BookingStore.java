package com.qlkhachsan.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.qlkhachsan.model.BookingByGuest;
import com.qlkhachsan.model.BookingByHotelDate;
import com.qlkhachsan.model.BookingById;
import com.qlkhachsan.model.BookingStatus;

/**
 * Cong truy cap du lieu dat phong (port).
 *
 * <p>Mot don dat phong duoc ghi (nhan ban) vao 3 bang de phuc vu 3 truy van
 * khac nhau - dung dac trung denormalized cua Cassandra:</p>
 * <ul>
 *   <li>{@code bookings_by_hotel_date} - le tan xem phong da dat theo ngay (Q4)</li>
 *   <li>{@code bookings_by_guest}     - lich su dat phong cua khach (Q3)</li>
 *   <li>{@code bookings_by_id}        - xem chi tiet / sua / xoa theo booking_id</li>
 * </ul>
 *
 * <p>{@link BookingById} duoc dung nhu ban ghi "phang" nguon: adapter Cassandra
 * se tu sinh khoa cho 2 bang con lai tu ban ghi nay.</p>
 */
public interface BookingStore {

    Optional<BookingById> findById(UUID bookingId);

    /** Danh sach tong hop (adapter Cassandra se fan-out theo tung khach san). */
    List<BookingById> findAll();

    List<BookingByGuest> findByGuest(String guestId);

    List<BookingByHotelDate> findByHotelBetween(String hotelId, LocalDate from, LocalDate to);

    /** Ghi don dat phong vao ca 3 bang. */
    BookingById save(BookingById booking);

    /** Xoa don dat phong o ca 3 bang. */
    boolean deleteById(UUID bookingId);

    /** Cap nhat trang thai don dat phong o ca 3 bang. */
    void updateStatus(UUID bookingId, BookingStatus status);

    long count();
}