package com.qlkhachsan.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Cac phep tinh tien cua hoa don - ham thuan (pure), dung chung cho
 * {@code DataSeeder} (du lieu mau) va {@code InvoiceService} (nghiep vu that)
 * de dam bao chi co MOT cong thuc duy nhat.
 *
 * <p>Quy uoc tien VND: lam tron 0 chu so thap phan (HALF_UP).</p>
 */
public final class InvoiceMath {

    /** Ty le phi dich vu tren tien phong. */
    public static final BigDecimal DEFAULT_SERVICE_RATE = new BigDecimal("0.05");

    /** Thue VAT mac dinh. */
    public static final BigDecimal DEFAULT_VAT_RATE = new BigDecimal("0.10");

    private InvoiceMath() {
    }

    /** So dem = so ngay giua check-in va check-out (toi thieu 1). */
    public static int nights(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
        return days < 0 ? 0 : (int) days;
    }

    /** Tien phong = gia phong x so dem. */
    public static BigDecimal roomCharge(BigDecimal pricePerNight, int nights) {
        if (pricePerNight == null || nights <= 0) {
            return BigDecimal.ZERO.setScale(0, RoundingMode.HALF_UP);
        }
        return pricePerNight.multiply(BigDecimal.valueOf(nights)).setScale(0, RoundingMode.HALF_UP);
    }

    /** Phi dich vu = tien phong x ty le. */
    public static BigDecimal serviceCharge(BigDecimal roomCharge, BigDecimal serviceRate) {
        if (roomCharge == null) {
            return BigDecimal.ZERO.setScale(0, RoundingMode.HALF_UP);
        }
        BigDecimal rate = serviceRate == null ? DEFAULT_SERVICE_RATE : serviceRate;
        return roomCharge.multiply(rate).setScale(0, RoundingMode.HALF_UP);
    }

    /** Thue = (tien phong + phi dich vu) x thue suat. */
    public static BigDecimal tax(BigDecimal roomCharge, BigDecimal serviceCharge, BigDecimal vatRate) {
        BigDecimal base = (roomCharge == null ? BigDecimal.ZERO : roomCharge)
                .add(serviceCharge == null ? BigDecimal.ZERO : serviceCharge);
        BigDecimal rate = vatRate == null ? DEFAULT_VAT_RATE : vatRate;
        return base.multiply(rate).setScale(0, RoundingMode.HALF_UP);
    }

    /** Tong tien = tien phong + phi dich vu + thue. */
    public static BigDecimal total(BigDecimal roomCharge, BigDecimal serviceCharge, BigDecimal tax) {
        return (roomCharge == null ? BigDecimal.ZERO : roomCharge)
                .add(serviceCharge == null ? BigDecimal.ZERO : serviceCharge)
                .add(tax == null ? BigDecimal.ZERO : tax)
                .setScale(0, RoundingMode.HALF_UP);
    }
}