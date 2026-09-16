package com.qlkhachsan.config;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.qlkhachsan.model.AppUser;
import com.qlkhachsan.model.BookingById;
import com.qlkhachsan.model.BookingStatus;
import com.qlkhachsan.model.Guest;
import com.qlkhachsan.model.Hotel;
import com.qlkhachsan.model.HotelStatus;
import com.qlkhachsan.model.Invoice;
import com.qlkhachsan.model.InvoiceKey;
import com.qlkhachsan.model.PaymentMethod;
import com.qlkhachsan.model.PaymentStatus;
import com.qlkhachsan.model.Room;
import com.qlkhachsan.model.RoomKey;
import com.qlkhachsan.model.RoomStatus;
import com.qlkhachsan.model.UserRole;
import com.qlkhachsan.repository.BookingStore;
import com.qlkhachsan.repository.GuestStore;
import com.qlkhachsan.repository.HotelStore;
import com.qlkhachsan.repository.InvoiceStore;
import com.qlkhachsan.repository.RoomStore;
import com.qlkhachsan.repository.UserStore;
import com.qlkhachsan.util.InvoiceMath;

/**
 * Tao du lieu mau khi kho du lieu con rong.
 *
 * <p>Chay duoc o CA HAI profile vi chi lam viec qua cac Store (port):
 * profile {@code memory} (mac dinh) va profile {@code cassandra} (Astra DB).
 * Tat seed bang {@code app.seed-on-startup=false}.</p>
 */
@Component
@ConditionalOnProperty(name = "app.seed-on-startup", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    /** Mui gio Viet Nam - dung de quy doi ngay nghiep vu sang Instant. */
    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final HotelStore hotelStore;
    private final RoomStore roomStore;
    private final GuestStore guestStore;
    private final BookingStore bookingStore;
    private final InvoiceStore invoiceStore;
    private final UserStore userStore;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataSeeder(HotelStore hotelStore, RoomStore roomStore, GuestStore guestStore,
                      BookingStore bookingStore, InvoiceStore invoiceStore, UserStore userStore) {
        this.hotelStore = hotelStore;
        this.roomStore = roomStore;
        this.guestStore = guestStore;
        this.bookingStore = bookingStore;
        this.invoiceStore = invoiceStore;
        this.userStore = userStore;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userStore.count() > 0 || hotelStore.count() > 0) {
            log.info("Bo qua tao du lieu mau: kho du lieu da co du lieu.");
            return;
        }
        log.info("Kho du lieu rong -> tao du lieu mau...");
        seedUsers();
        seedHotelsAndRooms();
        seedGuests();
        seedBookings();
        log.info("Da tao du lieu mau: {} khach san, {} phong, {} khach hang, {} don dat phong, {} hoa don, {} tai khoan",
                hotelStore.count(), roomStore.count(), guestStore.count(),
                bookingStore.count(), invoiceStore.count(), userStore.count());
    }

    /* =====================================================================
     * 1. TAI KHOAN (dang nhap / dang ky)
     * ===================================================================== */
    private void seedUsers() {
        createUser("admin", "admin123", "Nguyễn Quản Trị", "admin@qlkhachsan.vn", UserRole.ADMIN);
        createUser("manager", "manager123", "Trần Thị Quản Lý", "manager@qlkhachsan.vn", UserRole.MANAGER);
        createUser("staff", "staff123", "Lê Văn Lễ Tân", "staff@qlkhachsan.vn", UserRole.STAFF);
    }

    private void createUser(String username, String rawPassword, String fullName, String email, UserRole role) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role);
        user.setEnabled(true);
        user.setCreatedAt(Instant.now());
        userStore.save(user);
    }

    /* =====================================================================
     * 2. KHACH SAN + PHONG
     * ===================================================================== */
    private static final String[][] HOTEL_SEEDS = {
            {"KS001", "Grand Hotel TP.HCM", "TP.HCM", "123 Nguyễn Huệ, Quận 1", "5", "0283822111", "contact@grandhotel.vn", "ACTIVE", "10"},
            {"KS002", "Sunshine Hotel Đà Nẵng", "Đà Nng", "25 Võ Nguyên Giáp, Sơn Trà", "4", "0236388222", "info@sunshine.vn", "ACTIVE", "8"},
            {"KS003", "Royal Hotel Hà Nội", "Hà Nội", "88 Hàng Bạc, Hoàn Kiếm", "4", "0243822333", "sales@royal.vn", "INACTIVE", "6"},
            {"KS004", "Hotel Garden Sài Gòn", "TP.HCM", "45 Lê Lợi, Quận 3", "3", "0283822444", "booking@garden.vn", "ACTIVE", "6"},
            {"KS005", "Blue Sea Nha Trang", "Nha Trang", "12 Trần Phú, Lộc Thọ", "4", "0258382555", "hi@bluesea.vn", "ACTIVE", "6"},
            {"KS006", "Lotus Hotel Huế", "Huế", "9 Nguyễn Huệ, Phú Hòa", "3", "0234382666", "contact@lotus.vn", "ACTIVE", "4"}
    };

    private void seedHotelsAndRooms() {
        for (String[] seed : HOTEL_SEEDS) {
            Hotel hotel = new Hotel();
            hotel.setHotelId(seed[0]);
            hotel.setHotelName(seed[1]);
            hotel.setCity(seed[2]);
            hotel.setAddress(seed[3]);
            hotel.setStarRating(Integer.parseInt(seed[4]));
            hotel.setPhone(seed[5]);
            hotel.setEmail(seed[6]);
            hotel.setStatus(HotelStatus.valueOf(seed[7]));
            int roomCount = Integer.parseInt(seed[8]);
            hotel.setTotalRooms(roomCount);
            hotel.setCreatedAt(Instant.now());
            hotelStore.save(hotel);

            seedRoomsFor(hotel.getHotelId(), roomCount);
        }
    }

    /**
     * Sinh phong cho mot khach san: toi da 4 phong/tang, so phong dang 101, 102...
     * Loai phong va gia luan phien Standard / Deluxe / Suite. Phong cuoi cung cua
     * moi khach san duoc dat BAO TRI de man hinh loc co du lieu.
     */
    private void seedRoomsFor(String hotelId, int roomCount) {
        for (int i = 0; i < roomCount; i++) {
            int floor = i / 4 + 1;
            int roomNumber = floor * 100 + (i % 4 + 1);

            String roomType;
            BigDecimal price;
            int capacity;
            switch (i % 3) {
                case 0 -> {
                    roomType = "Standard";
                    price = new BigDecimal("500000");
                    capacity = 2;
                }
                case 1 -> {
                    roomType = "Deluxe";
                    price = new BigDecimal("800000");
                    capacity = 2;
                }
                default -> {
                    roomType = "Suite";
                    price = new BigDecimal("1500000");
                    capacity = 4;
                }
            }

            Room room = new Room(hotelId, roomNumber);
            room.setRoomType(roomType);
            room.setPricePerNight(price);
            room.setCapacity(capacity);
            room.setFloor(floor);
            room.setStatus(i == roomCount - 1 ? RoomStatus.MAINTENANCE : RoomStatus.AVAILABLE);
            room.setCreatedAt(Instant.now());
            roomStore.save(room);
        }
    }

    /* =====================================================================
     * 3. KHACH HANG
     * ===================================================================== */
    private static final String[][] GUEST_SEEDS = {
            {"Nguyễn Văn An", "0901234567", "an@gmail.com", "079123456789", "123 Lê Lợi, Quận 1, TP.HCM"},
            {"Trần Thị Bình", "0912345678", "binh@gmail.com", "079987654321", "45 Bạch Đằng, Đà Nẵng"},
            {"Lê Minh Hoàng", "0987654321", "hoang@gmail.com", "079456123789", "88 Hàng Bạc, Hà Nội"},
            {"Phạm Thu Hà", "0938123456", "ha@gmail.com", "079345678912", "210 Nguyễn Trãi, Quận 1, TP.HCM"}
    };

    private void seedGuests() {
        for (String[] seed : GUEST_SEEDS) {
            Guest guest = new Guest();
            guest.setFullName(seed[0]);
            guest.setPhone(seed[1]);
            guest.setEmail(seed[2]);
            guest.setCccd(seed[3]);
            guest.setAddress(seed[4]);
            guest.setCreatedAt(Instant.now());
            guestStore.save(guest); // tu sinh ma KH001, KH002...
        }
    }

    /* =====================================================================
     * 4. DON DAT PHONG + HOA DON
     * ===================================================================== */
    private void seedBookings() {
        LocalDate today = LocalDate.now(ZONE);

        // DP1 - da xac nhan, sap nhan phong: KS001 - Deluxe 102
        createBooking("KH001", "KS001", 102, today, today.plusDays(3), BookingStatus.CONFIRMED);

        // DP2 - khach DANG o: KS002 - Deluxe 201, hoa don chua thanh toan
        BookingById staying = createBooking("KH002", "KS002", 201,
                today.minusDays(1), today.plusDays(2), BookingStatus.CHECKED_IN);
        if (staying != null) {
            issueInvoice(staying, PaymentStatus.UNPAID, null);
        }

        // DP3 - cho xac nhan: KS001 - Deluxe 201
        createBooking("KH003", "KS001", 201, today.plusDays(4), today.plusDays(6), BookingStatus.PENDING);

        // DP4 - don da huy: KS004 - Standard 101
        createBooking("KH004", "KS004", 101, today.plusDays(1), today.plusDays(3), BookingStatus.CANCELLED);

        // DP5 - da tra phong, da thanh toan (thang nay): KS005 - Standard 101
        BookingById paidThisMonth = createBooking("KH001", "KS005", 101,
                today.minusDays(10), today.minusDays(7), BookingStatus.CHECKED_OUT);
        if (paidThisMonth != null) {
            issueInvoice(paidThisMonth, PaymentStatus.PAID, Instant.now());
        }

        // DP6 - da tra phong, da thanh toan thang truoc: KS001 - Standard 101
        LocalDate lastMonth = today.minusMonths(1);
        BookingById paidLastMonth = createBooking("KH002", "KS001", 101,
                lastMonth.withDayOfMonth(2), lastMonth.withDayOfMonth(4), BookingStatus.CHECKED_OUT);
        if (paidLastMonth != null) {
            issueInvoice(paidLastMonth, PaymentStatus.PAID, Instant.now());
        }

        // DP7 - da xac nhan tuan sau: KS006 - Deluxe 102
        createBooking("KH003", "KS006", 102, today.plusDays(7), today.plusDays(10), BookingStatus.CONFIRMED);
    }

    /**
     * Tao mot don dat phong day du (tu sinh booking_id) va dong bo trang
     * thai phong theo trang thai don: CONFIRMED -> DA DAT, CHECKED_IN -> DANG O.
     */
    private BookingById createBooking(String guestId, String hotelId, int roomNumber,
                                      LocalDate checkIn, LocalDate checkOut, BookingStatus status) {
        Guest guest = guestStore.findById(guestId).orElse(null);
        Hotel hotel = hotelStore.findById(hotelId).orElse(null);
        Room room = roomStore.findById(new RoomKey(hotelId, roomNumber)).orElse(null);
        if (guest == null || hotel == null || room == null) {
            return null;
        }

        int nights = InvoiceMath.nights(checkIn, checkOut);
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
        booking.setTotalAmount(InvoiceMath.roomCharge(room.getPricePerNight(), nights));
        booking.setStatus(status);
        booking.setCreatedAt(Instant.now());
        bookingStore.save(booking);

        switch (status) {
            case CONFIRMED -> {
                room.setStatus(RoomStatus.BOOKED);
                roomStore.save(room);
            }
            case CHECKED_IN -> {
                room.setStatus(RoomStatus.OCCUPIED);
                roomStore.save(room);
            }
            default -> {
                // trang thai khac: phong giu nguyen
            }
        }
        return booking;
    }

    /**
     * Phat sinh hoa don cho don dat phong theo cong thuc duy nhat
     * {@link InvoiceMath}: tien phong + phi dich vu 5% + VAT 10%.
     */
    private void issueInvoice(BookingById booking, PaymentStatus paymentStatus, Instant paidAt) {
        Room room = roomStore.findById(new RoomKey(booking.getHotelId(), booking.getRoomNumber())).orElse(null);
        BigDecimal roomCharge = InvoiceMath.roomCharge(
                room == null ? booking.getTotalAmount() : room.getPricePerNight(), booking.getNights());
        BigDecimal serviceCharge = InvoiceMath.serviceCharge(roomCharge, InvoiceMath.DEFAULT_SERVICE_RATE);
        BigDecimal tax = InvoiceMath.tax(roomCharge, serviceCharge, InvoiceMath.DEFAULT_VAT_RATE);

        Invoice invoice = new Invoice();
        invoice.setKey(new InvoiceKey(booking.getBookingId(), null)); // invoice_id tu sinh khi save
        invoice.setGuestId(booking.getGuestId());
        invoice.setGuestName(booking.getGuestName());
        invoice.setHotelId(booking.getHotelId());
        invoice.setRoomCharge(roomCharge);
        invoice.setServiceCharge(serviceCharge);
        invoice.setTax(tax);
        invoice.setTotalAmount(InvoiceMath.total(roomCharge, serviceCharge, tax));
        invoice.setPaymentStatus(paymentStatus);
        invoice.setPaidAt(paidAt);
        invoice.setDueDate(booking.getCheckOutDate());
        invoice.setIssuedAt(Instant.now());
        invoiceStore.save(invoice);
    }
}