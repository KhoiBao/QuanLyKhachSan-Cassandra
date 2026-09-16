package com.qlkhachsan.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.qlkhachsan.model.BookingById;
import com.qlkhachsan.model.BookingStatus;
import com.qlkhachsan.model.Guest;
import com.qlkhachsan.repository.BookingStore;
import com.qlkhachsan.repository.GuestStore;

/**
 * Nghiep vu khach hang: tim kiem (ten/SDT/email/CCCD), them/sua/xoa.
 * SDT khong duoc trung (chi muc guests_by_phone).
 */
@Service
public class GuestService {

    private final GuestStore guestStore;
    private final BookingStore bookingStore;

    public GuestService(GuestStore guestStore, BookingStore bookingStore) {
        this.guestStore = guestStore;
        this.bookingStore = bookingStore;
    }

    public List<Guest> search(String keyword) {
        String kw = keyword == null ? null : keyword.trim().toLowerCase(Locale.ROOT);
        List<Guest> result = new ArrayList<>();
        for (Guest guest : guestStore.findAll()) {
            if (kw != null && !kw.isBlank()
                    && !contains(guest.getFullName(), kw)
                    && !contains(guest.getPhone(), kw)
                    && !contains(guest.getEmail(), kw)
                    && !contains(guest.getCccd(), kw)
                    && !contains(guest.getGuestId(), kw)) {
                continue;
            }
            result.add(guest);
        }
        return result;
    }

    public Guest getById(String guestId) {
        return guestStore.findById(guestId)
                .orElseThrow(() -> new ValidationException("Không tìm thấy khách hàng " + guestId));
    }

    public void create(Guest form) {
        validate(form);
        form.setGuestId(null); // tu sinh KHxxx
        form.setCreatedAt(java.time.Instant.now());
        guestStore.save(form);
    }

    public void update(String guestId, Guest form) {
        validate(form);
        Guest guest = getById(guestId);
        Guest samePhone = guestStore.findByPhone(form.getPhone()).orElse(null);
        if (samePhone != null && !guestId.equals(samePhone.getGuestId())) {
            throw new ValidationException("Số điện thoại đã được dùng bởi " + samePhone.getDisplayName());
        }
        guest.setFullName(form.getFullName());
        guest.setPhone(form.getPhone());
        guest.setEmail(form.getEmail());
        guest.setCccd(form.getCccd());
        guest.setAddress(form.getAddress());
        guestStore.save(guest);
    }

    public void delete(String guestId) {
        Guest guest = getById(guestId);
        for (BookingById booking : bookingStore.findAll()) {
            boolean active = booking.getStatus() != BookingStatus.CANCELLED
                    && booking.getStatus() != BookingStatus.CHECKED_OUT;
            if (guestId.equals(booking.getGuestId()) && active) {
                throw new ValidationException("Không thể xóa khách hàng đang có đơn đặt phòng.");
            }
        }
        guestStore.deleteById(guest.getGuestId());
    }

    private void validate(Guest guest) {
        if (guest.getFullName() == null || guest.getFullName().isBlank()) {
            throw new ValidationException("Họ tên không được để trống.");
        }
        String phone = guest.getPhone() == null ? "" : guest.getPhone().replaceAll("[^0-9]", "");
        if (phone.length() < 9 || phone.length() > 12) {
            throw new ValidationException("Số điện thoại phải gồm 9-12 chữ số.");
        }
        guest.setPhone(phone);
        if (guest.getCccd() != null && !guest.getCccd().isBlank()) {
            String cccd = guest.getCccd().replaceAll("[^0-9]", "");
            if (!cccd.isEmpty() && cccd.length() != 12) {
                throw new ValidationException("CCCD phải gồm đúng 12 chữ số.");
            }
            guest.setCccd(cccd);
        }
    }

    private static boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
