package com.qlkhachsan.model;

/**
 * Vai tro nguoi dung (phan quyen).
 * Cot: users_by_username.role  ('ADMIN' | 'MANAGER' | 'STAFF')
 */
public enum UserRole {

    ADMIN("Quản trị viên"),
    MANAGER("Quản lý khách sạn"),
    STAFF("Lễ tân");

    private final String label;

    UserRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Ten quyen dung cho Spring Security (ROLE_xxx). */
    public String getAuthority() {
        return "ROLE_" + name();
    }

    /** Quyen quan ly danh muc khach san / phong. */
    public boolean canManageHotels() {
        return this == ADMIN || this == MANAGER;
    }

    /** Quyen xoa du lieu danh muc (chi ADMIN). */
    public boolean canDelete() {
        return this == ADMIN;
    }

    /** Quyen xem bao cao doanh thu. */
    public boolean canViewReports() {
        return this == ADMIN || this == MANAGER;
    }

    /** Quyen hoan tien hoa don. */
    public boolean canRefund() {
        return this == ADMIN || this == MANAGER;
    }
}