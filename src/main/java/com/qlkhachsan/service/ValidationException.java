package com.qlkhachsan.service;

/**
 * Ngoai le nghiep vu: du lieu khong hop le hoac khong thoa dieu kien
 * (phong da co nguoi dat, khach chua ton tai, ngay khong hop le...).
 *
 * <p>Controller se bat ngoai le nay, hien thong bao len giao dien
 * thay vi de exceptions cho ra trang 500.</p>
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
