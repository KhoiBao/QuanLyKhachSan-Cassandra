package com.qlkhachsan.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.qlkhachsan.model.BookingById;
import com.qlkhachsan.model.BookingByHotelDate;
import com.qlkhachsan.model.BookingStatus;
import com.qlkhachsan.model.Guest;
import com.qlkhachsan.model.Hotel;
import com.qlkhachsan.model.Room;
import com.qlkhachsan.model.RoomKey;
import com.qlkhachsan.model.RoomStatus;
import com.qlkhachsan.repository.BookingStore;
import com.qlkhachsan.repository.GuestStore;
import com.qlkhachsan.repository.HotelStore;
import com.qlkhachsan.repository.InvoiceStore;
import com.qlkhachsan.repository.RoomStore;
import com.qlkhachsan.util.InvoiceMath;

/**
 * Nghiep vu dat phong.
 *
 * <ul>
 *   <li>Tao don: kiem tra khach/khach san/phong, chong trung lich (overlap),
 *       tu tinh so dem + tong tien theo gia phong.</li>
 *   <li>Doi trang thai theo machine cua {@link BookingStatus#nextAllowed()},
 *       dong bo trang thai phong (BOOKED/OCCUPIED/AVAILABLE).</li>
 *   <li>Check-out: tu phat sinh hoa don (neu chua co) qua InvoiceService.</li>
 * </ul>
 */
@Service
public class BookingService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final BookingStore bookingStore;
    private final GuestStore guestStore;
    private final HotelStore hotelStore;
    private final RoomStore roomStore;
    private final InvoiceService invoiceService;

    public BookingService(BookingStore bookingStore, GuestStore guestStore, HotelStore hotelStore,
                          RoomStore roomStore, InvoiceService invoiceService) {
        this.bookingStore = bookingStore;
        this.guestStore = guestStore;
        this.hotelStore = hotelStore;
        this.roomStore = roomStore;
        this.invoiceService = invoiceService;
    }

    /** Loc don dat phong theo khach san / trang thai / khoang ngay / tu khoa. */
    public List<BookingById> search(String hotelId, BookingStatus status,
                                    LocalDate from, LocalDate to, String keyword) {
        List<BookingById> result = new ArrayList<>();
        String kw = keyword == null ? null : keyword.trim().toLowerCase();
        for (BookingById booking : bookingStore.findAll()) {
            if (hotelId != null && !hotelId.isBlank() && !hotelId.equals(booking.getHotelId())) {
                continue;
            }
            if (status != null && booking.getStatus() != status) {
                continue;
            }
            if (booking.getCheckInDate() != null) {
                if (from != null && booking.getCheckInDate().isBefore(from)) {
                    continue;
                }
                if (to != null && booking.getCheckInDate().isAfter(to)) {
                    continue;
                }
            }
            if (kw != null && !kw.isBlank()
                    && !contains(booking.getGuestName(), kw)
                    && !contains(booking.getHotelName(), kw)
                    && !contains(booking.getShortCode(), kw.toLowerCase())) {
                continue;
            }
            result.add(booking);
        }
        return result;
    }

    public BookingById getById(UUID bookingId) {
        return bookingStore.findById(bookingId)
                .orElseThrow(() -> new ValidationException("Không tìm thấy đơn đặt phòng."));
    }

    /**
     * Tao don dat phong moi.
     *
     * @return don da luu (co booking_id)
     * @throws ValidationException neu du lieu khong hop le hoac phong bi trung lich
     */
    public BookingById create(String guestId, String hotelId, Integer roomNumber,
                              LocalDate checkIn, LocalDate checkOut, String notes) {
        if (guestId == null || guestId.isBlank()) {
            throw new ValidationException("Hãy chọn khách hàng.");
        }
        if (hotelId == null || hotelId.isBlank()) {
            throw new ValidationException("Hãy chọn khách sạn.");
        }
        if (roomNumber == null) {
            throw new ValidationException("Hãy chọn phòng.");
        }
        if (checkIn == null || checkOut == null) {
            throw new ValidationException("Hãy chọn ngày nhận và ngày trả phòng.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new ValidationException("Ngày trả phòng phải sau ngày nhận phòng.");
        }
        if (checkIn.isBefore(LocalDate.now(ZONE))) {
            throw new ValidationException("Ngày nhận phòng không thể ở trong quá khứ.");
        }

        Guest guest = guestStore.findById(guestId)
                .orElseThrow(() -> new ValidationException("Khách hàng không tồn tại."));
        Hotel hotel = hotelStore.findById(hotelId)
                .orElseThrow(() -> new ValidationException("Khách sạn không tồn tại."));
        Room room = roomStore.findById(new RoomKey(hotelId, roomNumber))
                .orElseThrow(() -> new ValidationException("Phòng không tồn tại."));
        if (!room.getStatus().isBookable()) {
            throw new ValidationException("Phòng " + roomNumber + " hiện không thể đặt ("
                    + room.getStatus().getLabel() + ").");
        }
        ensureNoOverlap(hotelId, roomNumber, checkIn, checkOut, null);

        int nights = InvoiceMath.nights(checkIn, checkOut);
        BigDecimal total = InvoiceMath.roomCharge(room.getPricePerNight(), nights);

        BookingById booking = new BookingById();
        booking.setGuestId(guest.getGuestId());
        booking.setGuestName(guest.getFullName());
        booking.setHotelId(hotel.getHotelId());
        booking.setHotelName(hotel.getHotelName());
        booking.setRoomNumber(roomNumber);
        booking.setRoomType(room.getRoomType());
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setNights(nights);
        booking.setTotalAmount(total);
        booking.setStatus(BookingStatus.PENDING);
        booking.setNotes(notes);
        booking.setCreatedAt(java.time.Instant.now());
        bookingStore.save(booking);

        room.setStatus(RoomStatus.BOOKED);
        roomStore.save(room);
        return booking;
    }

    /**
     * Chuyen trang thai don theo luong cho phep, va dong bo phong:
     * CONFIRMED -> BOOKED, CHECKED_IN -> OCCUPIED, CHECKED_OUT/CANCELLED -> AVAILABLE.
     * CHECKED_OUT tu phat sinh hoa don neu chua co.
     */
    public void changeStatus(UUID bookingId, BookingStatus target) {
        if (target == null) {
            throw new ValidationException("Trạng thái không hợp lệ.");
        }
        BookingById booking = getById(bookingId);
        if (!booking.getStatus().nextAllowed().contains(target)) {
            throw new ValidationException("Không thể chuyển từ \""
                    + booking.getStatus().getLabel() + "\" sang \"" + target.getLabel() + "\".");
        }

        booking.setStatus(target);
        bookingStore.save(booking);

        Room room = roomStore.findById(new RoomKey(booking.getHotelId(), booking.getRoomNumber())).orElse(null);
        switch (target) {
            case CONFIRMED -> setRoomStatus(room, RoomStatus.BOOKED);
            case CHECKED_IN -> setRoomStatus(room, RoomStatus.OCCUPIED);
            case CHECKED_OUT -> {
                setRoomStatus(room, RoomStatus.AVAILABLE);
                if (invoiceService.findByBooking(bookingId).isEmpty()) {
                    invoiceService.createForBooking(booking);
                }
            }
            case CANCELLED -> setRoomStatus(room, RoomStatus.AVAILABLE);
            default -> {
                // PENDING: giu nguyen
            }
        }
    }

    /** Xoa don (kem hoa don lien quan) va tra phong ve AVAILABLE. */
    public void delete(UUID bookingId) {
        BookingById booking = getById(bookingId);
        invoiceService.deleteByBooking(bookingId);
        if (booking.getStatus().isActive()) {
            Room room = roomStore.findById(new RoomKey(booking.getHotelId(), booking.getRoomNumber())).orElse(null);
            setRoomStatus(room, RoomStatus.AVAILABLE);
        }
        bookingStore.deleteById(bookingId);
    }

    /** Chan dat trung: 2 don ACTIVE cung phong va cung thoi gian. */
    private void ensureNoOverlap(String hotelId, Integer roomNumber,
                                 LocalDate checkIn, LocalDate checkOut, UUID ignoreBookingId) {
        for (BookingByHotelDate other : bookingStore.findByHotelBetween(hotelId, checkIn.minusDays(366), checkOut)) {
            if (other.getBookingId() == null || other.getBookingId().equals(ignoreBookingId)) {
                continue;
            }
            boolean holding = other.getStatus() == BookingStatus.PENDING
                    || other.getStatus() == BookingStatus.CONFIRMED
                    || other.getStatus() == BookingStatus.CHECKED_IN;
            if (!holding || !roomNumber.equals(other.getRoomNumber())) {
                continue;
            }
            boolean overlaps = other.getCheckInDate().isBefore(checkOut)
                    && other.getCheckOutDate() != null
                    && checkIn.isBefore(other.getCheckOutDate());
            if (overlaps) {
                throw new ValidationException("Phòng " + roomNumber + " đã có đơn từ "
                        + other.getCheckInDate() + " đến " + other.getCheckOutDate() + ".");
            }
        }
    }

    private void setRoomStatus(Room room, RoomStatus status) {
        if (room != null) {
            room.setStatus(status);
            roomStore.save(room);
        }
    }

    private static boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }
}
