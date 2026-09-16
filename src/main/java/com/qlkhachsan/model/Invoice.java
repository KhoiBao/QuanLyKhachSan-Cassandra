package com.qlkhachsan.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Hoa don theo luot dat phong - bang {@code invoices_by_booking} (Q5).
 *
 * <pre>
 * PRIMARY KEY (booking_id, invoice_id)
 * </pre>
 */
@Table("invoices_by_booking")
public class Invoice {

    @PrimaryKey
    private InvoiceKey key = new InvoiceKey();

    @Column("guest_id")
    private String guestId;

    @Column("hotel_id")
    private String hotelId;

    @Column("room_charge")
    private BigDecimal roomCharge = BigDecimal.ZERO;

    @Column("service_charge")
    private BigDecimal serviceCharge = BigDecimal.ZERO;

    @Column("tax")
    private BigDecimal tax = BigDecimal.ZERO;

    @Column("total_amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column("payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column("issued_at")
    private Instant issuedAt = Instant.now();

    // ===== BO SUNG =====
    @Column("guest_name")
    private String guestName;

    @Column("due_date")
    private LocalDate dueDate;

    @Column("paid_at")
    private Instant paidAt;

    @Column("payment_method")
    private PaymentMethod paymentMethod;

    /** Ten khach san - chi dung de hien thi. */
    @Transient
    private String hotelName;

    public Invoice() {
    }

    /* ================== TIEN ICH ================== */

    @Transient
    public UUID getInvoiceId() {
        return key == null ? null : key.getInvoiceId();
    }

    @Transient
    public UUID getBookingId() {
        return key == null ? null : key.getBookingId();
    }

    /**
     * Khoa dinh danh hoa don dang chuoi: "bookingId|invoiceId".
     * Dung cho URL / form vi hoa don co khoa chinh gom 2 phan.
     */
    @Transient
    public String getCompositeId() {
        return (getBookingId() == null ? "" : getBookingId()) + "|" + (getInvoiceId() == null ? "" : getInvoiceId());
    }

    /** Ma hoa don hien thi, vi du "HD-3F2A1B". */
    @Transient
    public String getShortCode() {
        return getInvoiceId() == null ? "" : "HD-" + getInvoiceId().toString().substring(0, 8).toUpperCase();
    }

    @Transient
    public boolean isPayable() {
        return paymentStatus != null && paymentStatus.isPayable();
    }

    /* ================== GETTER / SETTER ================== */

    public InvoiceKey getKey() {
        return key;
    }

    public void setKey(InvoiceKey key) {
        this.key = key;
    }

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

    public BigDecimal getRoomCharge() {
        return roomCharge;
    }

    public void setRoomCharge(BigDecimal roomCharge) {
        this.roomCharge = roomCharge;
    }

    public BigDecimal getServiceCharge() {
        return serviceCharge;
    }

    public void setServiceCharge(BigDecimal serviceCharge) {
        this.serviceCharge = serviceCharge;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    @Override
    public String toString() {
        return "Invoice{key=" + key + ", totalAmount=" + totalAmount + ", paymentStatus=" + paymentStatus + "}";
    }
}