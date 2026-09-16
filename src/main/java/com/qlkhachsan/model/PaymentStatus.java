package com.qlkhachsan.model;

/**
 * Trang thai thanh toan hoa don.
 * Cot: invoices_by_booking.payment_status  ('UNPAID' | 'PAID' | 'REFUNDED')
 */
public enum PaymentStatus {

    UNPAID("Chưa thanh toán", "pending"),
    PAID("Đã thanh toán", "confirmed"),
    REFUNDED("Đã hoàn tiền", "cancelled");

    private final String label;
    private final String cssClass;

    PaymentStatus(String label, String cssClass) {
        this.label = label;
        this.cssClass = cssClass;
    }

    public String getLabel() {
        return label;
    }

    public String getCssClass() {
        return cssClass;
    }

    /** Chi hoa don UNPAID moi duoc xac nhan thanh toan. */
    public boolean isPayable() {
        return this == UNPAID;
    }

    /** Chi hoa don PAID moi duoc hoan tien. */
    public boolean isRefundable() {
        return this == PAID;
    }

    /** Doanh thu chi tinh tren hoa don da thu tien. */
    public boolean isRevenue() {
        return this == PAID;
    }
}