package com.qlkhachsan.model;

/**
 * Phuong thuc thanh toan.
 * Cot: invoices_by_booking.payment_method  ('CASH' | 'CARD' | 'TRANSFER')
 */
public enum PaymentMethod {

    CASH("Tiền mặt"),
    CARD("Thẻ ngân hàng"),
    TRANSFER("Chuyển khoản");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}