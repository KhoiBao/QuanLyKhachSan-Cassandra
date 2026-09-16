package com.qlkhachsan.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.qlkhachsan.model.Hotel;
import com.qlkhachsan.model.Invoice;
import com.qlkhachsan.model.PaymentStatus;
import com.qlkhachsan.repository.HotelStore;
import com.qlkhachsan.repository.InvoiceStore;

/**
 * Bao cao doanh thu: tong hop theo khach san va theo thang
 * tu cac hoa don DA THANH TOAN (PaymentStatus.PAID).
 */
@Service
public class ReportService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final InvoiceStore invoiceStore;
    private final HotelStore hotelStore;

    public ReportService(InvoiceStore invoiceStore, HotelStore hotelStore) {
        this.invoiceStore = invoiceStore;
        this.hotelStore = hotelStore;
    }

    /** Mot dong doanh thu theo khach san. */
    public record HotelRevenue(String hotelId, String hotelName, long invoiceCount, BigDecimal revenue) {
    }

    /** Mot cot doanh thu theo thang. */
    public record MonthRevenue(String label, BigDecimal revenue) {
    }

    /** Doanh thu theo tung khach san (giam dan). */
    public List<HotelRevenue> revenueByHotel() {
        Map<String, BigDecimal> revenue = new LinkedHashMap<>();
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Invoice invoice : invoiceStore.findAll()) {
            if (!invoice.getPaymentStatus().isRevenue() || invoice.getHotelId() == null) {
                continue;
            }
            revenue.merge(invoice.getHotelId(), invoice.getTotalAmount(), BigDecimal::add);
            counts.merge(invoice.getHotelId(), 1L, Long::sum);
        }
        List<HotelRevenue> rows = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : revenue.entrySet()) {
            Hotel hotel = hotelStore.findById(entry.getKey()).orElse(null);
            rows.add(new HotelRevenue(entry.getKey(),
                    hotel == null ? entry.getKey() : hotel.getHotelName(),
                    counts.getOrDefault(entry.getKey(), 0L), entry.getValue()));
        }
        rows.sort((a, b) -> b.revenue().compareTo(a.revenue()));
        return rows;
    }

    /** Doanh thu theo thang, tinh tu {@code months} thang gan nhat (moi nhat cuoi cung). */
    public List<MonthRevenue> revenueByMonth(int months) {
        LocalDate firstMonth = LocalDate.now(ZONE).minusMonths(months - 1L).withDayOfMonth(1);
        Map<String, BigDecimal> byMonth = new LinkedHashMap<>();
        DateTimeFormatter label = DateTimeFormatter.ofPattern("M/yyyy");
        for (int i = 0; i < months; i++) {
            byMonth.put(firstMonth.plusMonths(i).format(label), BigDecimal.ZERO);
        }
        for (Invoice invoice : invoiceStore.findAll()) {
            if (!invoice.getPaymentStatus().isRevenue() || invoice.getPaidAt() == null) {
                continue;
            }
            String key = LocalDate.ofInstant(invoice.getPaidAt(), ZONE).format(label);
            if (byMonth.containsKey(key)) {
                byMonth.merge(key, invoice.getTotalAmount(), BigDecimal::add);
            }
        }
        List<MonthRevenue> rows = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : byMonth.entrySet()) {
            String[] parts = entry.getKey().split("/");
            rows.add(new MonthRevenue("T" + parts[0] + "/" + parts[1], entry.getValue()));
        }
        return rows;
    }

    /** Tong doanh thu (PAID). */
    public BigDecimal totalRevenue() {
        BigDecimal total = BigDecimal.ZERO;
        for (Invoice invoice : invoiceStore.findAll()) {
            if (invoice.getPaymentStatus().isRevenue()) {
                total = total.add(invoice.getTotalAmount());
            }
        }
        return total;
    }

    /** Doanh thu hom nay (theo ngay thanh toan, gio Viet Nam). */
    public BigDecimal revenueToday() {
        Instant start = LocalDate.now(ZONE).atStartOfDay(ZONE).toInstant();
        BigDecimal total = BigDecimal.ZERO;
        for (Invoice invoice : invoiceStore.findAll()) {
            if (invoice.getPaymentStatus().isRevenue()
                    && invoice.getPaidAt() != null && !invoice.getPaidAt().isBefore(start)) {
                total = total.add(invoice.getTotalAmount());
            }
        }
        return total;
    }

    /** Doanh thu thang nay. */
    public BigDecimal revenueThisMonth() {
        Instant start = LocalDate.now(ZONE).withDayOfMonth(1).atStartOfDay(ZONE).toInstant();
        BigDecimal total = BigDecimal.ZERO;
        for (Invoice invoice : invoiceStore.findAll()) {
            if (invoice.getPaymentStatus().isRevenue()
                    && invoice.getPaidAt() != null && !invoice.getPaidAt().isBefore(start)) {
                total = total.add(invoice.getTotalAmount());
            }
        }
        return total;
    }

    /** Tong so hoa don da thanh toan. */
    public long paidInvoiceCount() {
        return invoiceStore.findAll().stream()
                .filter(i -> i.getPaymentStatus() == PaymentStatus.PAID)
                .count();
    }
}
