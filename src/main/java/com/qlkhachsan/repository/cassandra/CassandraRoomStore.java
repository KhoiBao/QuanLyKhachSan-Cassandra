package com.qlkhachsan.repository.cassandra;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.qlkhachsan.model.Room;
import com.qlkhachsan.model.RoomKey;
import com.qlkhachsan.model.RoomStatus;
import com.qlkhachsan.repository.RoomStore;

/**
 * Doc/ghi phong tren Astra DB - bang {@code rooms_by_hotel}
 * (partition key {@code hotel_id}).
 */
@Service
public class CassandraRoomStore implements RoomStore {

    private static final String COLS =
            "hotel_id, room_number, room_type, price_per_night, status, capacity, floor, created_at";

    private final CqlSession session;

    public CassandraRoomStore(CqlSession session) {
        this.session = session;
    }

    @Override
    public List<Room> findAll() {
        // Q: toan bo phong (quet nhieu partition - duoc vi du lieu nho)
        List<Room> list = new ArrayList<>();
        Cql.exec(session, "SELECT " + COLS + " FROM rooms_by_hotel").forEach(row -> list.add(toRoom(row)));
        list.sort(Comparator.comparing(Room::getHotelId, Comparator.nullsLast(String::compareTo))
                .thenComparing(Room::getRoomNumber, Comparator.nullsLast(Integer::compareTo)));
        return list;
    }

    @Override
    public List<Room> findByHotel(String hotelId) {
        // Q: phong cua 1 khach san (doc 1 partition - query hieu qua nhat)
        List<Room> list = new ArrayList<>();
        Cql.exec(session,
                "SELECT " + COLS + " FROM rooms_by_hotel WHERE hotel_id = ?", hotelId)
                .forEach(row -> list.add(toRoom(row)));
        list.sort(Comparator.comparing(Room::getRoomNumber, Comparator.nullsLast(Integer::compareTo)));
        return list;
    }

    @Override
    public Optional<Room> findById(RoomKey key) {
        if (key == null) {
            return Optional.empty();
        }
        // Q: 1 phong theo khoa day du (partition + clustering key)
        Row row = Cql.exec(session,
                "SELECT " + COLS + " FROM rooms_by_hotel WHERE hotel_id = ? AND room_number = ?",
                key.getHotelId(), key.getRoomNumber()).one();
        return Optional.ofNullable(row).map(CassandraRoomStore::toRoom);
    }

    @Override
    public Room save(Room room) {
        if (room == null || room.getHotelId() == null || room.getHotelId().isBlank()
                || room.getRoomNumber() == null) {
            throw new IllegalArgumentException("RoomKey (hotelId + roomNumber) khong duoc de trong.");
        }
        // Q: them/sua phong (upsert)
        Cql.exec(session,
                "INSERT INTO rooms_by_hotel (" + COLS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                room.getHotelId(), room.getRoomNumber(), room.getRoomType(), room.getPricePerNight(),
                room.getStatus() == null ? null : room.getStatus().name(),
                room.getCapacity(), room.getFloor(), room.getCreatedAt());
        return room;
    }

    @Override
    public boolean deleteById(RoomKey key) {
        if (key == null) {
            return false;
        }
        // Q: xoa 1 phong
        Cql.exec(session, "DELETE FROM rooms_by_hotel WHERE hotel_id = ? AND room_number = ?",
                key.getHotelId(), key.getRoomNumber());
        return findById(key).isEmpty();
    }

    @Override
    public void deleteByHotel(String hotelId) {
        if (hotelId == null) {
            return;
        }
        // Q: xoa ca partition (toan bo phong cua khach san)
        Cql.exec(session, "DELETE FROM rooms_by_hotel WHERE hotel_id = ?", hotelId);
    }

    @Override
    public long count() {
        Row row = Cql.exec(session, "SELECT COUNT(*) FROM rooms_by_hotel").one();
        return row == null ? 0 : row.getLong(0);
    }

    private static Room toRoom(Row row) {
        Room room = new Room();
        room.setKey(new RoomKey(row.getString("hotel_id"),
                row.isNull("room_number") ? null : row.getInt("room_number")));
        room.setRoomType(row.getString("room_type"));
        room.setPricePerNight(row.getBigDecimal("price_per_night"));
        String status = row.getString("status");
        try {
            room.setStatus(status == null ? RoomStatus.AVAILABLE : RoomStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            room.setStatus(RoomStatus.AVAILABLE);
        }
        room.setCapacity(row.isNull("capacity") ? null : row.getInt("capacity"));
        room.setFloor(row.isNull("floor") ? null : row.getInt("floor"));
        room.setCreatedAt(row.getInstant("created_at"));
        return room;
    }
}
