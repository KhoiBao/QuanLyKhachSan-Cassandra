package com.qlkhachsan.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.qlkhachsan.model.BookingById;
import com.qlkhachsan.model.Room;
import com.qlkhachsan.model.RoomStatus;
import com.qlkhachsan.repository.BookingStore;
import com.qlkhachsan.repository.GuestStore;
import com.qlkhachsan.repository.HotelStore;
import com.qlkhachsan.repository.InvoiceStore;
import com.qlkhachsan.repository.RoomStore;

/**
 * So lieu tong quan cho dashboard: so khach san / phong / khach / don,
 * doanh thu, ty le trang thai phong, danh sach don gan nhat.
 */
@Service
public class DashboardService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final HotelStore hotelStore;
    private final RoomStore roomStore;
    private final GuestStore guestStore;
    private final BookingStore bookingStore;
    private final InvoiceStore invoiceStore;

    public DashboardService(HotelStore hotelStore, RoomStore roomStore, GuestStore guestStore,
                            BookingStore bookingStore, InvoiceStore invoiceStore) {
        this.hotelStore = hotelStore;
        this.roomStore = roomStore;
        this.guestStore = guestStore;
        this.bookingStore = bookingStore;
        this.invoiceStore = invoiceStore;
    }

    public long totalHotels() {
        return hotelStore.count();
    }

    public long activeHotels() {
        return hotelStore.countByStatus(com.qlkhachsan.model.HotelStatus.ACTIVE);
    }

    public long totalRooms() {
        return roomStore.count();
    }

    public long availableRooms() {
        return roomStore.findAll().stream().filter(r -> r.getStatus() == RoomStatus.AVAILABLE).count();
    }

    public long totalGuests() {
        return guestStore.count();
    }

    public long totalBookings() {
        return bookingStore.count();
    }

    /** So don ACTIVE (da xac nhan / dang o). */
    public long activeBookings() {
        return bookingStore.findAll().stream().filter(b -> b.getStatus().isActive()).count();
    }

    /** So don tao trong thang hien tai. */
    public long bookingsThisMonth() {
        Instant start = LocalDate.now(ZONE).withDayOfMonth(1).atStartOfDay(ZONE).toInstant();
        return bookingStore.findAll().stream()
                .filter(b -> b.getCreatedAt() != null && !b.getCreatedAt().isBefore(start))
                .count();
    }

    /** Dem so phong theo tung trang thai (dung cho khoi Trang thai phong). */
    public Map<RoomStatus, Long> roomStatusCounts() {
        Map<RoomStatus, Long> counts = new LinkedHashMap<>();
        for (RoomStatus status : RoomStatus.values()) {
            counts.put(status, 0L);
        }
        for (Room room : roomStore.findAll()) {
            counts.merge(room.getStatus(), 1L, Long::sum);
        }
        return counts;
    }

    /** Top don dat phong moi nhat (theo thoi diem tao). */
    public List<BookingById> recentBookings(int limit) {
        List<BookingById> bookings = new ArrayList<>(bookingStore.findAll());
        bookings.sort(Comparator.comparing(BookingById::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return bookings.subList(0, Math.min(limit, bookings.size()));
    }

    /** Tong doanh thu da thu (PAID) - de dua vao dashboard. */
    public BigDecimal totalRevenue() {
        BigDecimal total = BigDecimal.ZERO;
        for (com.qlkhachsan.model.Invoice invoice : invoiceStore.findAll()) {
            if (invoice.getPaymentStatus().isRevenue()) {
                total = total.add(invoice.getTotalAmount());
            }
        }
        return total;
    }
}
