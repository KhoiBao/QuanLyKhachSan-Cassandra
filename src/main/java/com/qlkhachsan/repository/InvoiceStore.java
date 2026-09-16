package com.qlkhachsan.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.qlkhachsan.model.Invoice;
import com.qlkhachsan.model.InvoiceKey;

/**
 * Cong truy cap du lieu hoa don (port).
 *
 * <p>Bang {@code invoices_by_booking} co partition key la {@code booking_id}
 * nen {@link #findByBooking(UUID)} la truy van 1 partition.</p>
 */
public interface InvoiceStore {

    List<Invoice> findAll();

    Optional<Invoice> findById(InvoiceKey key);

    List<Invoice> findByBooking(UUID bookingId);

    Invoice save(Invoice invoice);

    /** Xoa toan bo hoa don cua mot don dat phong. */
    void deleteByBooking(UUID bookingId);

    long count();
}