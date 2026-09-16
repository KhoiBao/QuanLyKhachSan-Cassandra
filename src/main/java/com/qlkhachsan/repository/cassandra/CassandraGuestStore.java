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
import com.qlkhachsan.model.Guest;
import com.qlkhachsan.repository.GuestStore;

/**
 * Doc/ghi khach hang tren Astra DB.
 * <ul>
 *   <li>Bang chinh: {@code guests}</li>
 *   <li>Bang index: {@code guests_by_phone} (tim nhanh theo SDT)</li>
 * </ul>
 */
@Service
public class CassandraGuestStore implements GuestStore {

    private static final Pattern ID_PATTERN = Pattern.compile("KH(\\d+)");
    private static final String COLS = "guest_id, full_name, phone, email, cccd, address, created_at";

    private final CqlSession session;

    public CassandraGuestStore(CqlSession session) {
        this.session = session;
    }

    @Override
    public List<Guest> findAll() {
        // Q: toan bo khach hang
        List<Guest> list = new ArrayList<>();
        Cql.exec(session, "SELECT " + COLS + " FROM guests").forEach(row -> list.add(toGuest(row)));
        list.sort(Comparator.comparing(Guest::getGuestId, Comparator.nullsLast(String::compareTo)));
        return list;
    }

    @Override
    public Optional<Guest> findById(String guestId) {
        if (guestId == null) {
            return Optional.empty();
        }
        // Q: 1 khach hang theo id
        Row row = Cql.exec(session,
                "SELECT " + COLS + " FROM guests WHERE guest_id = ?", guestId).one();
        return Optional.ofNullable(row).map(CassandraGuestStore::toGuest);
    }

    @Override
    public Optional<Guest> findByPhone(String phone) {
        if (phone == null) {
            return Optional.empty();
        }
        // Q: qua bang index SDT truoc...
        Row idx = Cql.exec(session,
                "SELECT guest_id FROM guests_by_phone WHERE phone = ?", phone).one();
        if (idx != null && idx.getString("guest_id") != null) {
            Optional<Guest> guest = findById(idx.getString("guest_id"));
            if (guest.isPresent()) {
                return guest;
            }
        }
        // ...fallback quet nhe neu index lech
        String digits = phone.replaceAll("[^0-9]", "");
        return findAll().stream()
                .filter(g -> g.getPhone() != null && g.getPhone().replaceAll("[^0-9]", "").equals(digits))
                .findFirst();
    }

    @Override
    public Guest save(Guest guest) {
        if (guest == null) {
            throw new IllegalArgumentException("Guest khong duoc null.");
        }
        if (guest.getGuestId() == null || guest.getGuestId().isBlank()) {
            guest.setGuestId(nextGuestId());
        }
        // Neu doi SDT -> xoa dong index SDT cu
        findById(guest.getGuestId()).ifPresent(old -> {
            if (old.getPhone() != null && !old.getPhone().equals(guest.getPhone())) {
                Cql.exec(session, "DELETE FROM guests_by_phone WHERE phone = ?", old.getPhone());
            }
        });
        // Q: ghi bang chinh + bang index SDT (2 lenh)
        Cql.exec(session,
                "INSERT INTO guests (" + COLS + ") VALUES (?, ?, ?, ?, ?, ?, ?)",
                guest.getGuestId(), guest.getFullName(), guest.getPhone(), guest.getEmail(),
                guest.getCccd(), guest.getAddress(), guest.getCreatedAt());
        if (guest.getPhone() != null && !guest.getPhone().isBlank()) {
            Cql.exec(session,
                    "INSERT INTO guests_by_phone (phone, guest_id, full_name, email, created_at) "
                    + "VALUES (?, ?, ?, ?, ?)",
                    guest.getPhone(), guest.getGuestId(), guest.getFullName(),
                    guest.getEmail(), guest.getCreatedAt());
        }
        return guest;
    }

    @Override
    public boolean deleteById(String guestId) {
        if (guestId == null) {
            return false;
        }
        // Q: xoa ca bang chinh + index SDT
        findById(guestId).ifPresent(old -> {
            if (old.getPhone() != null) {
                Cql.exec(session, "DELETE FROM guests_by_phone WHERE phone = ?", old.getPhone());
            }
        });
        Cql.exec(session, "DELETE FROM guests WHERE guest_id = ?", guestId);
        return findById(guestId).isEmpty();
    }

    @Override
    public long count() {
        Row row = Cql.exec(session, "SELECT COUNT(*) FROM guests").one();
        return row == null ? 0 : row.getLong(0);
    }

    @Override
    public String nextGuestId() {
        int max = 0;
        for (Guest guest : findAll()) {
            if (guest.getGuestId() == null) {
                continue;
            }
            Matcher m = ID_PATTERN.matcher(guest.getGuestId().trim().toUpperCase());
            if (m.matches()) {
                try {
                    max = Math.max(max, Integer.parseInt(m.group(1)));
                } catch (NumberFormatException ignored) {
                    // bo qua
                }
            }
        }
        return String.format("KH%03d", max + 1);
    }

    private static Guest toGuest(Row row) {
        Guest guest = new Guest();
        guest.setGuestId(row.getString("guest_id"));
        guest.setFullName(row.getString("full_name"));
        guest.setPhone(row.getString("phone"));
        guest.setEmail(row.getString("email"));
        guest.setCccd(row.getString("cccd"));
        guest.setAddress(row.getString("address"));
        guest.setCreatedAt(row.getInstant("created_at"));
        return guest;
    }
}
