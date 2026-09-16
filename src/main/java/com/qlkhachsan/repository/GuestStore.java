package com.qlkhachsan.repository;

import java.util.List;
import java.util.Optional;

import com.qlkhachsan.model.Guest;

/**
 * Cong truy cap du lieu khach hang (port).
 *
 * <p>Ngoai bang {@code guests} con co bang tra cuu {@code guests_by_phone}
 * de tim nhanh khach hang theo so dien thoai (le tan nhap so dien thoai).</p>
 */
public interface GuestStore {

    List<Guest> findAll();

    Optional<Guest> findById(String guestId);

    Optional<Guest> findByPhone(String phone);

    Guest save(Guest guest);

    boolean deleteById(String guestId);

    long count();

    /** Sinh ma khach hang tiep theo (KH001, KH002...). */
    String nextGuestId();
}