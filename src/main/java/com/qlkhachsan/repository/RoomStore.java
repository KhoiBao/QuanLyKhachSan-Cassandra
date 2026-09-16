package com.qlkhachsan.repository;

import java.util.List;
import java.util.Optional;

import com.qlkhachsan.model.Room;
import com.qlkhachsan.model.RoomKey;

/**
 * Cong truy cap du lieu phong (port).
 *
 * <p>Bang {@code rooms_by_hotel} co partition key la {@code hotel_id} nen
 * {@link #findByHotel(String)} la truy van hieu qua nhat (doc 1 partition).</p>
 */
public interface RoomStore {

    /** Doc toan bo phong cua moi khach san (adapter Cassandra se fan-out theo tung hotel). */
    List<Room> findAll();

    List<Room> findByHotel(String hotelId);

    Optional<Room> findById(RoomKey key);

    Room save(Room room);

    boolean deleteById(RoomKey key);

    /** Xoa toan bo phong cua mot khach san (dung khi xoa khach san). */
    void deleteByHotel(String hotelId);

    long count();
}