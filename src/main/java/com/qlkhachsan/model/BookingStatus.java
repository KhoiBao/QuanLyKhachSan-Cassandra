package com.qlkhachsan.model;

import java.util.List;

/**
 * Trang thai don dat phong.
 * Cot: bookings_by_guest.status / bookings_by_hotel_date.status / bookings_by_id.status
 * ('PENDING' | 'CONFIRMED' | 'CHECKED_IN' | 'CHECKED_OUT' | 'CANCELLED')
 */
public enum BookingStatus {

    PENDING("Chờ xác nhận", "pending"),
    CONFIRMED("Đã xác nhận", "confirmed"),
    CHECKED_IN("Đã nhận phòng", "info"),
    CHECKED_OUT("Đã trả phòng", "completed"),
    CANCELLED("Đã hủy", "cancelled");

    private final String label;
    private final String cssClass;

    BookingStatus(String label, String cssClass) {
        this.label = label;
        this.cssClass = cssClass;
    }

    public String getLabel() {
        return label;
    }

    public String getCssClass() {
        return cssClass;
    }

    /** Trang thai tiep theo duoc phep chuyen sang (rong = da ket thuc luong). */
    public List<BookingStatus> nextAllowed() {
        return switch (this) {
            case PENDING -> List.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> List.of(CHECKED_IN, CANCELLED);
            case CHECKED_IN -> List.of(CHECKED_OUT);
            case CHECKED_OUT, CANCELLED -> List.of();
        };
    }

    /** Don da xac nhan/da nhan phong thi khong duoc sua ngay phong nua. */
    public boolean isEditable() {
        return this == PENDING || this == CONFIRMED;
    }

    /** Don da huy hoac da tra phong = da ket thuc. */
    public boolean isFinished() {
        return this == CHECKED_OUT || this == CANCELLED;
    }

    /** Don dang chiem phong (dung de tinh trang thai phong). */
    public boolean isActive() {
        return this == CONFIRMED || this == CHECKED_IN;
    }
}