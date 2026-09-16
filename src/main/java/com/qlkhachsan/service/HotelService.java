package com.qlkhachsan.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.qlkhachsan.model.BookingById;
import com.qlkhachsan.model.Hotel;
import com.qlkhachsan.model.HotelStatus;
import com.qlkhachsan.repository.BookingStore;
import com.qlkhachsan.repository.HotelStore;
import com.qlkhachsan.repository.RoomStore;

/**
 * Nghiep vu khach san: tim kiem / them / sua / xoa.
 *
 * <p>Xoa khach san phai xoa luon phong (deleteByHotel) va bi chan neu
 * van con don dat phong dang chua (PENDING/CONFIRMED/CHECKED_IN).</p>
 */
@Service
public class HotelService {

    private final HotelStore hotelStore;
    private final RoomStore roomStore;
    private final BookingStore bookingStore;

    public HotelService(HotelStore hotelStore, RoomStore roomStore, BookingStore bookingStore) {
        this.hotelStore = hotelStore;
        this.roomStore = roomStore;
        this.bookingStore = bookingStore;
    }

    /** Tim kiem theo tu khoa (ten/dia chi/SDT) + thanh pho + trang thai. */
    public List<Hotel> search(String keyword, String city, HotelStatus status) {
        List<Hotel> result = new ArrayList<>();
        String kw = lower(keyword);
        for (Hotel hotel : hotelStore.findAll()) {
            if (status != null && hotel.getStatus() != status) {
                continue;
            }
            if (city != null && !city.isBlank() && !city.equals(hotel.getCity())) {
                continue;
            }
            if (kw != null && !contains(hotel.getHotelName(), kw) && !contains(hotel.getAddress(), kw)
                    && !contains(hotel.getPhone(), kw) && !contains(hotel.getHotelId(), kw)) {
                continue;
            }
            result.add(hotel);
        }
        return result;
    }

    /** Danh sach thanh pho co khach san (dung cho dropdown loc). */
    public List<String> cities() {
        List<String> cities = new ArrayList<>();
        for (Hotel hotel : hotelStore.findAll()) {
            if (hotel.getCity() != null && !hotel.getCity().isBlank() && !cities.contains(hotel.getCity())) {
                cities.add(hotel.getCity());
            }
        }
        return cities;
    }

    public Hotel getById(String hotelId) {
        return hotelStore.findById(hotelId)
                .orElseThrow(() -> new ValidationException("Không tìm thấy khách sạn " + hotelId));
    }

    public void create(Hotel form) {
        validate(form);
        form.setHotelId(hotelStore.nextHotelId()); // KSxxx tu sinh
        form.setCreatedAt(java.time.Instant.now());
        hotelStore.save(form);
    }

    public void update(String hotelId, Hotel form) {
        validate(form);
        Hotel hotel = getById(hotelId);
        hotel.setHotelName(form.getHotelName());
        hotel.setCity(form.getCity());
        hotel.setAddress(form.getAddress());
        hotel.setStarRating(form.getStarRating());
        hotel.setPhone(form.getPhone());
        hotel.setEmail(form.getEmail());
        hotel.setStatus(form.getStatus() == null ? hotel.getStatus() : form.getStatus());
        hotelStore.save(hotel);
    }

    public void delete(String hotelId) {
        Hotel hotel = getById(hotelId);
        for (BookingById booking : bookingStore.findAll()) {
            if (hotelId.equals(booking.getHotelId()) && booking.getStatus().isActive()) {
                throw new ValidationException("Không thể xóa khách sạn đang có đơn đặt phòng chưa kết thúc.");
            }
        }
        roomStore.deleteByHotel(hotelId);
        hotelStore.deleteById(hotel.getHotelId());
    }

    /* ================== KIEM TRA DU LIEU ================== */

    private void validate(Hotel hotel) {
        require(hotel.getHotelName(), "Tên khách sạn");
        require(hotel.getCity(), "Thành phố");
        require(hotel.getAddress(), "Địa chỉ");
        if (hotel.getStarRating() == null || hotel.getStarRating() < 1 || hotel.getStarRating() > 5) {
            throw new ValidationException("Số sao phải từ 1 đến 5.");
        }
        if (hotel.getStatus() == null) {
            hotel.setStatus(HotelStatus.ACTIVE);
        }
    }

    private void require(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(label + " không được để trống.");
        }
    }

    private static boolean contains(String value, String keyword) {
        return value != null && lower(value).contains(keyword);
    }

    private static String lower(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
