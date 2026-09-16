package com.qlkhachsan.repository;

import java.util.List;
import java.util.Optional;

import com.qlkhachsan.model.Hotel;
import com.qlkhachsan.model.HotelStatus;

/**
 * Cong truy cap du lieu khach san (port).
 *
 * <p>Co 2 hien thuc (adapter):</p>
 * <ul>
 *   <li>{@code InMemoryHotelStore}  - profile {@code memory} (mac dinh)</li>
 *   <li>{@code CassandraHotelStore} - profile {@code cassandra} (Spring Data Cassandra)</li>
 * </ul>
 *
 * <p>Tang service chi lam viec voi interface nay nen nghiep vu (tim kiem, loc,
 * phan trang, thong ke) chi viet mot lan cho ca hai che do luu tru.</p>
 */
public interface HotelStore {

    List<Hotel> findAll();

    Optional<Hotel> findById(String hotelId);

    Hotel save(Hotel hotel);

    boolean deleteById(String hotelId);

    boolean existsById(String hotelId);

    long count();

    long countByStatus(HotelStatus status);

    /** Sinh ma khach san tiep theo (KS001, KS002...) dua tren du lieu hien co. */
    String nextHotelId();
}