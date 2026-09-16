package com.qlkhachsan.repository.cassandra;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.qlkhachsan.model.Hotel;
import com.qlkhachsan.model.HotelStatus;
import com.qlkhachsan.repository.HotelStore;

/**
 * Doc/ghi khach san tren Astra DB - bang {@code hotels}.
 */
@Service
public class CassandraHotelStore implements HotelStore {

    private static final Pattern ID_PATTERN = Pattern.compile("KS(\\d+)");
    private static final String COLS =
            "hotel_id, hotel_name, city, address, star_rating, phone, email, status, total_rooms, created_at";

    private final CqlSession session;

    public CassandraHotelStore(CqlSession session) {
        this.session = session;
    }

    @Override
    public List<Hotel> findAll() {
        // Q: toan bo khach san (trang chu, dropdown loc)
        List<Hotel> list = new ArrayList<>();
        Cql.exec(session, "SELECT " + COLS + " FROM hotels").forEach(row -> list.add(toHotel(row)));
        list.sort(Comparator.comparing(Hotel::getHotelId, Comparator.nullsLast(String::compareTo)));
        return list;
    }

    @Override
    public Optional<Hotel> findById(String hotelId) {
        // Q: chi tiet 1 khach san theo partition key
        Row row = Cql.exec(session,
                "SELECT " + COLS + " FROM hotels WHERE hotel_id = ?", hotelId).one();
        return Optional.ofNullable(row).map(CassandraHotelStore::toHotel);
    }

    @Override
    public Hotel save(Hotel hotel) {
        if (hotel == null || hotel.getHotelId() == null || hotel.getHotelId().isBlank()) {
            throw new IllegalArgumentException("hotelId khong duoc de trong.");
        }
        // Q: them/sua khach san (upsert theo khoa chinh)
        Cql.exec(session,
                "INSERT INTO hotels (" + COLS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                hotel.getHotelId(), hotel.getHotelName(), hotel.getCity(), hotel.getAddress(),
                hotel.getStarRating(), hotel.getPhone(), hotel.getEmail(),
                hotel.getStatus() == null ? null : hotel.getStatus().name(),
                hotel.getTotalRooms(), hotel.getCreatedAt());
        return hotel;
    }

    @Override
    public boolean deleteById(String hotelId) {
        // Q: xoa khach san
        Cql.exec(session, "DELETE FROM hotels WHERE hotel_id = ?", hotelId);
        return !existsById(hotelId);
    }

    @Override
    public boolean existsById(String hotelId) {
        return hotelId != null && Cql.exec(session,
                "SELECT hotel_id FROM hotels WHERE hotel_id = ?", hotelId).one() != null;
    }

    @Override
    public long count() {
        Row row = Cql.exec(session, "SELECT COUNT(*) FROM hotels").one();
        return row == null ? 0 : row.getLong(0);
    }

    @Override
    public long countByStatus(HotelStatus status) {
        // status khong phai key -> quet + loc tren code
        return findAll().stream().filter(h -> h.getStatus() == status).count();
    }

    @Override
    public String nextHotelId() {
        int max = 0;
        for (Hotel hotel : findAll()) {
            if (hotel.getHotelId() == null) {
                continue;
            }
            Matcher m = ID_PATTERN.matcher(hotel.getHotelId().trim().toUpperCase());
            if (m.matches()) {
                try {
                    max = Math.max(max, Integer.parseInt(m.group(1)));
                } catch (NumberFormatException ignored) {
                    // bo qua id khong dung dinh dang
                }
            }
        }
        return String.format("KS%03d", max + 1);
    }

    private static Hotel toHotel(Row row) {
        Hotel hotel = new Hotel();
        hotel.setHotelId(row.getString("hotel_id"));
        hotel.setHotelName(row.getString("hotel_name"));
        hotel.setCity(row.getString("city"));
        hotel.setAddress(row.getString("address"));
        hotel.setStarRating(row.isNull("star_rating") ? null : row.getInt("star_rating"));
        hotel.setPhone(row.getString("phone"));
        hotel.setEmail(row.getString("email"));
        String status = row.getString("status");
        try {
            hotel.setStatus(status == null ? HotelStatus.ACTIVE : HotelStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            hotel.setStatus(HotelStatus.ACTIVE);
        }
        hotel.setTotalRooms(row.isNull("total_rooms") ? 0 : row.getInt("total_rooms"));
        hotel.setCreatedAt(row.getInstant("created_at"));
        return hotel;
    }
}
