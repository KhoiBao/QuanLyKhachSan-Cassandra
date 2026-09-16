package com.qlkhachsan.model;

/**
 * Trang thai hoat dong cua khach san.
 * Cot: hotels.status  ('ACTIVE' | 'INACTIVE')
 */
public enum HotelStatus {

    ACTIVE("Hoạt động", "confirmed"),
    INACTIVE("Tạm ngưng", "pending");

    private final String label;
    private final String cssClass;

    HotelStatus(String label, String cssClass) {
        this.label = label;
        this.cssClass = cssClass;
    }

    /** Nhan hien thi tieng Viet tren giao dien. */
    public String getLabel() {
        return label;
    }

    /** Class CSS cho badge (xem dashboard.css). */
    public String getCssClass() {
        return cssClass;
    }
}