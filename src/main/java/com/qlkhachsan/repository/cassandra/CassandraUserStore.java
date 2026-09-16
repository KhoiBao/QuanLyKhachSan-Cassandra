package com.qlkhachsan.repository.cassandra;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.qlkhachsan.model.AppUser;
import com.qlkhachsan.model.UserRole;
import com.qlkhachsan.repository.UserStore;

/**
 * Doc/ghi tai khoan tren Astra DB (keyspace khachsan).
 * <ul>
 *   <li>Bang chinh: {@code users_by_username} (dang nhap theo username)</li>
 *   <li>Bang index: {@code users_by_email} (chan trung email khi dang ky)</li>
 * </ul>
 */
@Service
public class CassandraUserStore implements UserStore {

    private final CqlSession session;

    public CassandraUserStore(CqlSession session) {
        this.session = session;
    }

    @Override
    public Optional<AppUser> findByUsername(String username) {
        // Q: dang nhap - tim theo partition key
        Row row = Cql.exec(session,
                "SELECT username, password_hash, full_name, email, role, enabled, created_at "
                + "FROM users_by_username WHERE username = ?", username).one();
        return Optional.ofNullable(row).map(CassandraUserStore::toUser);
    }

    @Override
    public Optional<AppUser> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        // Q: qua bang index truoc...
        Row idx = Cql.exec(session,
                "SELECT username FROM users_by_email WHERE email = ?", email.trim()).one();
        if (idx != null && idx.getString("username") != null) {
            Optional<AppUser> user = findByUsername(idx.getString("username"));
            if (user.isPresent()) {
                return user;
            }
        }
        // ...fallback quet nhe neu index lech
        for (AppUser user : findAll()) {
            if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(email.trim())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<AppUser> findAll() {
        // Q: liet ke tai khoan
        List<AppUser> list = new ArrayList<>();
        Cql.exec(session,
                "SELECT username, password_hash, full_name, email, role, enabled, created_at "
                + "FROM users_by_username").forEach(row -> list.add(toUser(row)));
        return list;
    }

    @Override
    public AppUser save(AppUser user) {
        if (user == null || user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username khong duoc de trong.");
        }
        // Neu doi email -> xoa dong index email cu keo rac
        findByUsername(user.getUsername()).ifPresent(old -> {
            if (old.getEmail() != null && !old.getEmail().equalsIgnoreCase(user.getEmail())) {
                Cql.exec(session, "DELETE FROM users_by_email WHERE email = ?", old.getEmail());
            }
        });
        // Q: ghi bang chinh + bang index (2 lenh)
        Cql.exec(session,
                "INSERT INTO users_by_username "
                + "(username, password_hash, full_name, email, role, enabled, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                user.getUsername(), user.getPasswordHash(), user.getFullName(), user.getEmail(),
                user.getRole() == null ? null : user.getRole().name(), user.getEnabled(), user.getCreatedAt());
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            Cql.exec(session,
                    "INSERT INTO users_by_email (email, username, full_name, role, created_at) "
                    + "VALUES (?, ?, ?, ?, ?)",
                    user.getEmail(), user.getUsername(), user.getFullName(),
                    user.getRole() == null ? null : user.getRole().name(), user.getCreatedAt());
        }
        return user;
    }

    @Override
    public boolean existsByUsername(String username) {
        return username != null && Cql.exec(session,
                "SELECT username FROM users_by_username WHERE username = ?", username).one() != null;
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public long count() {
        Row row = Cql.exec(session, "SELECT COUNT(*) FROM users_by_username").one();
        return row == null ? 0 : row.getLong(0);
    }

    private static AppUser toUser(Row row) {
        AppUser user = new AppUser();
        user.setUsername(row.getString("username"));
        user.setPasswordHash(row.getString("password_hash"));
        user.setFullName(row.getString("full_name"));
        user.setEmail(row.getString("email"));
        String role = row.getString("role");
        try {
            user.setRole(role == null ? UserRole.STAFF : UserRole.valueOf(role));
        } catch (IllegalArgumentException e) {
            user.setRole(UserRole.STAFF);
        }
        user.setEnabled(row.isNull("enabled") ? Boolean.TRUE : row.getBoolean("enabled"));
        user.setCreatedAt(row.getInstant("created_at"));
        return user;
    }
}
