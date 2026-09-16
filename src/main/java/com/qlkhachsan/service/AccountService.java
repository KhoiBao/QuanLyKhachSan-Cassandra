package com.qlkhachsan.service;

import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.qlkhachsan.model.AppUser;
import com.qlkhachsan.model.UserRole;
import com.qlkhachsan.repository.UserStore;

/**
 * Nghiep vu tai khoan: dang ky tai khoan moi (mac dinh vai tro STAFF).
 */
@Service
public class AccountService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.]{3,30}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final UserStore userStore;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserStore userStore, PasswordEncoder passwordEncoder) {
        this.userStore = userStore;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(String username, String rawPassword, String fullName, String email) {
        if (username == null || !USERNAME_PATTERN.matcher(username.trim()).matches()) {
            throw new ValidationException("Tên đăng nhập 3-30 ký tự, chỉ gồm chữ, số, dấu chấm và gạch dưới.");
        }
        if (userStore.existsByUsername(username.trim())) {
            throw new ValidationException("Tên đăng nhập đã tồn tại.");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new ValidationException("Mật khẩu phải có ít nhất 6 ký tự.");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Email không hợp lệ.");
        }
        if (userStore.existsByEmail(email.trim())) {
            throw new ValidationException("Email đã được dùng cho tài khoản khác.");
        }

        AppUser user = new AppUser();
        user.setUsername(username.trim());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName == null || fullName.isBlank() ? username.trim() : fullName.trim());
        user.setEmail(email.trim());
        user.setRole(UserRole.STAFF);
        user.setEnabled(Boolean.TRUE);
        user.setCreatedAt(java.time.Instant.now());
        userStore.save(user);
    }
}
