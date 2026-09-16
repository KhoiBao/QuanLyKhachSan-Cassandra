package com.qlkhachsan.repository.cassandra;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.qlkhachsan.model.BookingByGuest;
import com.qlkhachsan.model.BookingByGuestKey;
import com.qlkhachsan.model.BookingByHotelDate;
import com.qlkhachsan.model.BookingByHotelDateKey;
import com.qlkhachsan.model.BookingById;
import com.qlkhachsan.model.BookingStatus;
import com.qlkhachsan.repository.BookingStore;

/**
 * Doc/ghi don dat phong tren Astra DB - ghi 1 don vao ca 3 bang
 * (denormalized - dac trung Cassandra):
 * <ul>
 *   <li>{@code bookings_by_id} - xem/sua/xoa theo booking_id</li>
 *   <li>{@code bookings_by_guest} - lich su theo khach (partition guest_id)</li>
 *   <li>{@code bookings_by_hotel_date} - le tan xem theo ngay (partition hotel_id)</li>
 * </ul>
 */
@Service
public class CassandraBookingStore implements BookingStore {

    private static final String COLS_BY_ID =
            "booking_id, guest_id, guest_name, hotel_id, hotel_name, room_number, room_type, "
            + "check_in_date, check_out_date, nights, total_amount, status, notes, created_at";
    private static final String COLS_BY_GUEST =
            "guest_id, check_in_date, booking_id, hotel_id, hotel_name, guest_name, room_number, "
            + "room_type, check_out_date, nights, total_amount, status, notes, created_at";
    private static final String COLS_BY_HOTEL_DATE =
            "hotel_id, check_in_date, booking_id, guest_id, guest_name, room_number, room_type, "
            + "check_out_date, nights, total_amount, status, notes, created_at";

    private final CqlSession session;

    public CassandraBookingStore(CqlSession session) {
        this.session = session;
    }

    @Override
    public Optional<BookingById> findById(UUID bookingId) {
        if (bookingId == null) {
            return Optional.empty();
        }
        // Q: chi tiet don theo id
        Row row = Cql.exec(session,
                "SELECT " + COLS_BY_ID + " FROM bookings_by_id WHERE booking_id = ?", bookingId).one();
        return Optional.ofNullable(row).map(CassandraBookingStore::toById);
    }

    @Override
    public List<BookingById> findAll() {
        // Q: toan bo don (tong hop)
        List<BookingById> list = new ArrayList<>();
        Cql.exec(session, "SELECT " + COLS_BY_ID + " FROM bookings_by_id")
                .forEach(row -> list.add(toById(row)));
        list.sort(Comparator.comparing(BookingById::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    @Override
    public List<BookingByGuest> findByGuest(String guestId) {
        // Q: lich su dat phong cua 1 khach (doc 1 partition)
        List<BookingByGuest> list = new ArrayList<>();
        Cql.exec(session,
                "SELECT " + COLS_BY_GUEST + " FROM bookings_by_guest WHERE guest_id = ?", guestId)
                .forEach(row -> list.add(toByGuest(row)));
        list.sort(Comparator.comparing(BookingByGuest::getCheckInDate,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    @Override
    public List<BookingByHotelDate> findByHotelBetween(String hotelId, LocalDate from, LocalDate to) {
        // Q: don cua 1 khach san trong khoang ngay (partition + range tren clustering key)
        List<BookingByHotelDate> list = new ArrayList<>();
        if (from != null && to != null) {
            Cql.exec(session,
                    "SELECT " + COLS_BY_HOTEL_DATE + " FROM bookings_by_hotel_date "
                    + "WHERE hotel_id = ? AND check_in_date >= ? AND check_in_date <= ?",
                    hotelId, from, to).forEach(row -> list.add(toByHotelDate(row)));
        } else if (from != null) {
            Cql.exec(session,
                    "SELECT " + COLS_BY_HOTEL_DATE + " FROM bookings_by_hotel_date "
                    + "WHERE hotel_id = ? AND check_in_date >= ?", hotelId, from)
                    .forEach(row -> list.add(toByHotelDate(row)));
        } else if (to != null) {
            Cql.exec(session,
                    "SELECT " + COLS_BY_HOTEL_DATE + " FROM bookings_by_hotel_date "
                    + "WHERE hotel_id = ? AND check_in_date <= ?", hotelId, to)
                    .forEach(row -> list.add(toByHotelDate(row)));
        } else {
            Cql.exec(session,
                    "SELECT " + COLS_BY_HOTEL_DATE + " FROM bookings_by_hotel_date WHERE hotel_id = ?",
                    hotelId).forEach(row -> list.add(toByHotelDate(row)));
        }
        list.sort(Comparator.comparing(BookingByHotelDate::getCheckInDate,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return list;
    }

    @Override
    public BookingById save(BookingById booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking khong duoc null.");
        }
        if (booking.getBookingId() == null) {
            booking.setBookingId(UUID.randomUUID());
        }
        if (booking.getCreatedAt() == null) {
            booking.setCreatedAt(java.time.Instant.now());
        }
        // Neu sua lam doi khoa phu (khach/ngay/ks) -> xoa dong index cu keo rac
        findById(booking.getBookingId()).ifPresent(old -> {
            if (!Objects.equals(old.getGuestId(), booking.getGuestId())
                    || !Objects.equals(old.getCheckInDate(), booking.getCheckInDate())) {
                deleteGuestRow(old);
            }
            if (!Objects.equals(old.getHotelId(), booking.getHotelId())
                    || !Objects.equals(old.getCheckInDate(), booking.getCheckInDate())) {
                deleteHotelDateRow(old);
            }
        });
        // Q: ghi 1 don vao ca 3 bang (3 lenh)
        Cql.exec(session,
                "INSERT INTO bookings_by_id (" + COLS_BY_ID + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                booking.getBookingId(), booking.getGuestId(), booking.getGuestName(),
                booking.getHotelId(), booking.getHotelName(), booking.getRoomNumber(),
                booking.getRoomType(), booking.getCheckInDate(), booking.getCheckOutDate(),
                booking.getNights(), booking.getTotalAmount(), statusOf(booking),
                booking.getNotes(), booking.getCreatedAt());
        Cql.exec(session,
                "INSERT INTO bookings_by_guest (" + COLS_BY_GUEST + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                booking.getGuestId(), booking.getCheckInDate(), booking.getBookingId(),
                booking.getHotelId(), booking.getHotelName(), booking.getGuestName(),
                booking.getRoomNumber(), booking.getRoomType(), booking.getCheckOutDate(),
                booking.getNights(), booking.getTotalAmount(), statusOf(booking),
                booking.getNotes(), booking.getCreatedAt());
        Cql.exec(session,
                "INSERT INTO bookings_by_hotel_date (" + COLS_BY_HOTEL_DATE + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                booking.getHotelId(), booking.getCheckInDate(), booking.getBookingId(),
                booking.getGuestId(), booking.getGuestName(), booking.getRoomNumber(),
                booking.getRoomType(), booking.getCheckOutDate(), booking.getNights(),
                booking.getTotalAmount(), statusOf(booking), booking.getNotes(), booking.getCreatedAt());
        return booking;
    }

    @Override
    public boolean deleteById(UUID bookingId) {
        if (bookingId == null) {
            return false;
        }
        // Q: xoa don o ca 3 bang (can doc truoc de co du khoa phu)
        Optional<BookingById> existing = findById(bookingId);
        existing.ifPresent(old -> {
            deleteGuestRow(old);
            deleteHotelDateRow(old);
        });
        Cql.exec(session, "DELETE FROM bookings_by_id WHERE booking_id = ?", bookingId);
        return findById(bookingId).isEmpty();
    }

    @Override
    public void updateStatus(UUID bookingId, BookingStatus status) {
        if (bookingId == null || status == null) {
            return;
        }
        // Q: doi status o ca 3 bang (khoa phu khong doi nen UPDATE truc tiep)
        findById(bookingId).ifPresent(old -> {
            String name = status.name();
            Cql.exec(session, "UPDATE bookings_by_id SET status = ? WHERE booking_id = ?",
                    name, bookingId);
            Cql.exec(session,
                    "UPDATE bookings_by_guest SET status = ? WHERE guest_id = ? AND check_in_date = ? AND booking_id = ?",
                    name, old.getGuestId(), old.getCheckInDate(), bookingId);
            Cql.exec(session,
                    "UPDATE bookings_by_hotel_date SET status = ? WHERE hotel_id = ? AND check_in_date = ? AND booking_id = ?",
                    name, old.getHotelId(), old.getCheckInDate(), bookingId);
        });
    }

    @Override
    public long count() {
        Row row = Cql.exec(session, "SELECT COUNT(*) FROM bookings_by_id").one();
        return row == null ? 0 : row.getLong(0);
    }

    private void deleteGuestRow(BookingById old) {
        Cql.exec(session,
                "DELETE FROM bookings_by_guest WHERE guest_id = ? AND check_in_date = ? AND booking_id = ?",
                old.getGuestId(), old.getCheckInDate(), old.getBookingId());
    }

    private void deleteHotelDateRow(BookingById old) {
        Cql.exec(session,
                "DELETE FROM bookings_by_hotel_date WHERE hotel_id = ? AND check_in_date = ? AND booking_id = ?",
                old.getHotelId(), old.getCheckInDate(), old.getBookingId());
    }

    private static String statusOf(BookingById booking) {
        return booking.getStatus() == null ? null : booking.getStatus().name();
    }

    private static BookingStatus statusOf(String name, BookingStatus fallback) {
        if (name == null) {
            return fallback;
        }
        try {
            return BookingStatus.valueOf(name);
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private static BookingById toById(Row row) {
        BookingById b = new BookingById();
        b.setBookingId(row.getUuid("booking_id"));
        b.setGuestId(row.getString("guest_id"));
        b.setGuestName(row.getString("guest_name"));
        b.setHotelId(row.getString("hotel_id"));
        b.setHotelName(row.getString("hotel_name"));
        b.setRoomNumber(row.isNull("room_number") ? null : row.getInt("room_number"));
        b.setRoomType(row.getString("room_type"));
        b.setCheckInDate(row.getLocalDate("check_in_date"));
        b.setCheckOutDate(row.getLocalDate("check_out_date"));
        b.setNights(row.isNull("nights") ? null : row.getInt("nights"));
        b.setTotalAmount(row.getBigDecimal("total_amount"));
        b.setStatus(statusOf(row.getString("status"), BookingStatus.PENDING));
        b.setNotes(row.getString("notes"));
        b.setCreatedAt(row.getInstant("created_at"));
        return b;
    }

    private static BookingByGuest toByGuest(Row row) {
        BookingByGuest g = new BookingByGuest();
        g.setKey(new BookingByGuestKey(row.getString("guest_id"),
                row.getLocalDate("check_in_date"), row.getUuid("booking_id")));
        g.setHotelId(row.getString("hotel_id"));
        g.setHotelName(row.getString("hotel_name"));
        g.setGuestName(row.getString("guest_name"));
        g.setRoomNumber(row.isNull("room_number") ? null : row.getInt("room_number"));
        g.setRoomType(row.getString("room_type"));
        g.setCheckOutDate(row.getLocalDate("check_out_date"));
        g.setNights(row.isNull("nights") ? null : row.getInt("nights"));
        g.setTotalAmount(row.getBigDecimal("total_amount"));
        g.setStatus(statusOf(row.getString("status"), BookingStatus.PENDING));
        g.setNotes(row.getString("notes"));
        g.setCreatedAt(row.getInstant("created_at"));
        return g;
    }

    private static BookingByHotelDate toByHotelDate(Row row) {
        BookingByHotelDate h = new BookingByHotelDate();
        h.setKey(new BookingByHotelDateKey(row.getString("hotel_id"),
                row.getLocalDate("check_in_date"), row.getUuid("booking_id")));
        h.setGuestId(row.getString("guest_id"));
        h.setGuestName(row.getString("guest_name"));
        h.setRoomNumber(row.isNull("room_number") ? null : row.getInt("room_number"));
        h.setRoomType(row.getString("room_type"));
        h.setCheckOutDate(row.getLocalDate("check_out_date"));
        h.setNights(row.isNull("nights") ? null : row.getInt("nights"));
        h.setTotalAmount(row.getBigDecimal("total_amount"));
        h.setStatus(statusOf(row.getString("status"), BookingStatus.PENDING));
        h.setNotes(row.getString("notes"));
        h.setCreatedAt(row.getInstant("created_at"));
        return h;
    }
}
