package com.qlkhachsan.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

/**
 * Khoa chinh cua bang {@code invoices_by_booking}.
 *
 * <pre>
 * PRIMARY KEY (booking_id, invoice_id)
 * </pre>
 */
@PrimaryKeyClass
public class InvoiceKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @PrimaryKeyColumn(name = "booking_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID bookingId;

    @PrimaryKeyColumn(name = "invoice_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private UUID invoiceId;

    public InvoiceKey() {
    }

    public InvoiceKey(UUID bookingId, UUID invoiceId) {
        this.bookingId = bookingId;
        this.invoiceId = invoiceId;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public UUID getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InvoiceKey other)) {
            return false;
        }
        return Objects.equals(bookingId, other.bookingId) && Objects.equals(invoiceId, other.invoiceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId, invoiceId);
    }

    @Override
    public String toString() {
        return "InvoiceKey{bookingId=" + bookingId + ", invoiceId=" + invoiceId + "}";
    }
}