package com.qlkhachsan.repository;

import java.util.List;
import java.util.Optional;

import com.qlkhachsan.model.AppUser;

/**
 * Cong truy cap du lieu tai khoan nguoi dung (port) - phuc vu dang nhap/dang ky.
 *
 * <p>Hai bang: {@code users_by_username} (dang nhap) va {@code users_by_email}
 * (chan trung email khi dang ky).</p>
 */
public interface UserStore {

    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByEmail(String email);

    List<AppUser> findAll();

    AppUser save(AppUser user);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    long count();
}