package com.qlkhachsan.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.qlkhachsan.model.BookingById;
import com.qlkhachsan.model.Hotel;
import com.qlkhachsan.model.Invoice;
import com.qlkhachsan.model.InvoiceKey;
import com.qlkhachsan.model.PaymentMethod;
import com.qlkhachsan.model.PaymentStatus;
import com.qlkhachsan.model.Room;
import com.qlkhachsan.model.RoomKey;
import com.qlkhachsan.repository.BookingStore;
import com.qlkhachsan.repository.HotelStore;
import com.qlkhachsan.repository.InvoiceStore;
import com.qlkhachsan.repository.RoomStore;
import com.qlkhachsan.util.InvoiceMath;

/**
 * Nghiep vu hoa don: tao hoa don cho don dat phong (khi check-out),
 * thanh toan, hoan tien, xoa.
 *
 * <p>Cong thuc tien duy nhat: {@link InvoiceMath}
 * (tien phong + phi dich vu 5% + VAT 10%, lam tron VND).</p>
 */
@Service
public class InvoiceService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final InvoiceStore invoiceStore;
    private final BookingStore bookingStore;
    private final HotelStore hotelStore;
    private final RoomStore roomStore;

    public InvoiceService(InvoiceStore invoiceStore, BookingStore bookingStore,
                          HotelStore hotelStore, RoomStore roomStore) {
        this.invoiceStore = invoiceStore;
        this.bookingStore = bookingStore;
        this.hotelStore = hotelStore;
        this.roomStore = roomStore;
    }

    public List<Invoice> search(String keyword, PaymentStatus status) {
        String kw = keyword == null ? null : keyword.trim().toLowerCase();
        List<Invoice> result = new ArrayList<>();
        for (Invoice invoice : decorate(invoiceStore.findAll())) {
            if (status != null && invoice.getPaymentStatus() != status) {
                continue;
            }
            if (kw != null && !kw.isBlank()
                    && !contains(invoice.getGuestName(), kw)
                    && !contains(invoice.getHotelName(), kw)
                    && !contains(invoice.getShortCode(), kw)) {
                continue;
            }
            result.add(invoice);
        }
        return result;
    }

    public List<Invoice> findByBooking(UUID bookingId) {
        return invoiceStore.findByBooking(bookingId);
    }

    public Invoice findById(InvoiceKey key) {
        return invoiceStore.findById(key)
                .orElseThrow(() -> new ValidationException("Không tìm thấy hóa đơn."));
    }

    /** Tao hoa don cho mot don dat phong (dung khi check-out). */
    public Invoice createForBooking(BookingById booking) {
        Room room = roomStore.findById(new RoomKey(booking.getHotelId(), booking.getRoomNumber())).orElse(null);
        BigDecimal roomCharge = InvoiceMath.roomCharge(
                room == null ? booking.getTotalAmount() : room.getPricePerNight(), booking.getNights());
        BigDecimal serviceCharge = InvoiceMath.serviceCharge(roomCharge, InvoiceMath.DEFAULT_SERVICE_RATE);
        BigDecimal tax = InvoiceMath.tax(roomCharge, serviceCharge, InvoiceMath.DEFAULT_VAT_RATE);

        Invoice invoice = new Invoice();
        invoice.setKey(new InvoiceKey(booking.getBookingId(), null));
        invoice.setGuestId(booking.getGuestId());
        invoice.setGuestName(booking.getGuestName());
        invoice.setHotelId(booking.getHotelId());
        invoice.setRoomCharge(roomCharge);
        invoice.setServiceCharge(serviceCharge);
        invoice.setTax(tax);
        invoice.setTotalAmount(InvoiceMath.total(roomCharge, serviceCharge, tax));
        invoice.setPaymentStatus(PaymentStatus.UNPAID);
        invoice.setDueDate(booking.getCheckOutDate());
        invoice.setIssuedAt(Instant.now());
        return invoiceStore.save(invoice);
    }

    /** Thanh toan hoa don: UNPAID -> PAID, ghi nhan thoi gian + phuong thuc. */
    public void pay(UUID bookingId, UUID invoiceId, PaymentMethod method) {
        Invoice invoice = findById(new InvoiceKey(bookingId, invoiceId));
        if (!invoice.getPaymentStatus().isPayable()) {
            throw new ValidationException("Hóa đơn không ở trạng thái chờ thanh toán.");
        }
        invoice.setPaymentStatus(PaymentStatus.PAID);
        invoice.setPaymentMethod(method == null ? PaymentMethod.CASH : method);
        invoice.setPaidAt(Instant.now());
        invoiceStore.save(invoice);
    }

    /** Hoan tien hoa don: PAID -> REFUNDED. */
    public void refund(UUID bookingId, UUID invoiceId) {
        Invoice invoice = findById(new InvoiceKey(bookingId, invoiceId));
        if (!invoice.getPaymentStatus().isRefundable()) {
            throw new ValidationException("Chỉ hóa đơn đã thanh toán mới được hoàn tiền.");
        }
        invoice.setPaymentStatus(PaymentStatus.REFUNDED);
        invoiceStore.save(invoice);
    }

    public void deleteByBooking(UUID bookingId) {
        invoiceStore.deleteByBooking(bookingId);
    }

    /** Gan ten khach san de hien thi. */
    private List<Invoice> decorate(List<Invoice> invoices) {
        for (Invoice invoice : invoices) {
            hotelStore.findById(invoice.getHotelId())
                    .map(Hotel::getHotelName)
                    .ifPresent(invoice::setHotelName);
        }
        return invoices;
    }

    /** Tong doanh thu (chi hoa da PAID). */
    public BigDecimal totalRevenue() {
        BigDecimal total = BigDecimal.ZERO;
        for (Invoice invoice : invoiceStore.findAll()) {
            if (invoice.getPaymentStatus().isRevenue()) {
                total = total.add(invoice.getTotalAmount());
            }
        }
        return total;
    }

    /** Tong doanh thu trong thang hien tai (theo thoi diem thanh toan). */
    public BigDecimal revenueThisMonth() {
        return revenueSince(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).withDayOfMonth(1).atStartOfDay(ZONE).toInstant());
    }

    /** Tong doanh thu ke tu thoi diem cho truoc (chi PAID). */
    public BigDecimal revenueSince(Instant from) {
        BigDecimal total = BigDecimal.ZERO;
        for (Invoice invoice : invoiceStore.findAll()) {
            if (invoice.getPaymentStatus().isRevenue()
                    && invoice.getPaidAt() != null && !invoice.getPaidAt().isBefore(from)) {
                total = total.add(invoice.getTotalAmount());
            }
        }
        return total;
    }

    /** Don dat phong chua co hoa don (dung khi check-out). */
    public Invoice createForBookingById(UUID bookingId) {
        BookingById booking = bookingStore.findById(bookingId)
                .orElseThrow(() -> new ValidationException("Không tìm thấy đơn đặt phòng."));
        if (!findByBooking(bookingId).isEmpty()) {
            throw new ValidationException("Đơn này đã có hóa đơn.");
        }
        return createForBooking(booking);
    }

    private static boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }
}
