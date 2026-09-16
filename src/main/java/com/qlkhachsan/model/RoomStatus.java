package com.qlkhachsan.model;

/**
 * Trang thai phong.
 * Cot: rooms_by_hotel.status
 * ('AVAILABLE' | 'BOOKED' | 'OCCUPIED' | 'MAINTENANCE')
 */
public enum RoomStatus {

    AVAILABLE("Trống", "available"),
    BOOKED("Đã đặt", "pending"),
    OCCUPIED("Đang sử dụng", "occupied"),
    MAINTENANCE("Bảo trì", "maintenance");

    private final String label;
    private final String cssClass;

    RoomStatus(String label, String cssClass) {
        this.label = label;
        this.cssClass = cssClass;
    }

    public String getLabel() {
        return label;
    }

    public String getCssClass() {
        return cssClass;
    }

    /** Phong co the dat duoc khong. */
    public boolean isBookable() {
        return this == AVAILABLE;
    }
}