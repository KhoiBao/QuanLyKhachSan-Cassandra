package com.qlkhachsan.repository.cassandra;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.qlkhachsan.model.Invoice;
import com.qlkhachsan.model.InvoiceKey;
import com.qlkhachsan.model.PaymentMethod;
import com.qlkhachsan.model.PaymentStatus;
import com.qlkhachsan.repository.InvoiceStore;

/**
 * Doc/ghi hoa don tren Astra DB - bang {@code invoices_by_booking}
 * (partition key {@code booking_id}).
 */
@Service
public class CassandraInvoiceStore implements InvoiceStore {

    private static final String COLS =
            "booking_id, invoice_id, guest_id, guest_name, hotel_id, room_charge, service_charge, "
            + "tax, total_amount, payment_status, payment_method, due_date, issued_at, paid_at";

    private final CqlSession session;

    public CassandraInvoiceStore(CqlSession session) {
        this.session = session;
    }

    @Override
    public List<Invoice> findAll() {
        // Q: toan bo hoa don (bao cao doanh thu quet bang nay)
        List<Invoice> list = new ArrayList<>();
        Cql.exec(session, "SELECT " + COLS + " FROM invoices_by_booking")
                .forEach(row -> list.add(toInvoice(row)));
        list.sort(Comparator.comparing(Invoice::getIssuedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return list;
    }

    @Override
    public Optional<Invoice> findById(InvoiceKey key) {
        if (key == null) {
            return Optional.empty();
        }
        // Q: 1 hoa don theo khoa day du
        Row row = Cql.exec(session,
                "SELECT " + COLS + " FROM invoices_by_booking WHERE booking_id = ? AND invoice_id = ?",
                key.getBookingId(), key.getInvoiceId()).one();
        return Optional.ofNullable(row).map(CassandraInvoiceStore::toInvoice);
    }

    @Override
    public List<Invoice> findByBooking(UUID bookingId) {
        // Q: hoa don cua 1 don dat phong (doc 1 partition)
        List<Invoice> list = new ArrayList<>();
        if (bookingId == null) {
            return list;
        }
        Cql.exec(session,
                "SELECT " + COLS + " FROM invoices_by_booking WHERE booking_id = ?", bookingId)
                .forEach(row -> list.add(toInvoice(row)));
        return list;
    }

    @Override
    public Invoice save(Invoice invoice) {
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice khong duoc null.");
        }
        if (invoice.getKey() == null) {
            invoice.setKey(new InvoiceKey(null, UUID.randomUUID()));
        }
        InvoiceKey key = invoice.getKey();
        if (key.getBookingId() == null) {
            throw new IllegalArgumentException("bookingId cua hoa don khong duoc null.");
        }
        if (key.getInvoiceId() == null) {
            key.setInvoiceId(UUID.randomUUID());
        }
        if (invoice.getIssuedAt() == null) {
            invoice.setIssuedAt(java.time.Instant.now());
        }
        // Q: phat sinh / thanh toan / hoan tien hoa don (upsert)
        Cql.exec(session,
                "INSERT INTO invoices_by_booking (" + COLS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                key.getBookingId(), key.getInvoiceId(), invoice.getGuestId(), invoice.getGuestName(),
                invoice.getHotelId(), invoice.getRoomCharge(), invoice.getServiceCharge(),
                invoice.getTax(), invoice.getTotalAmount(),
                invoice.getPaymentStatus() == null ? null : invoice.getPaymentStatus().name(),
                invoice.getPaymentMethod() == null ? null : invoice.getPaymentMethod().name(),
                invoice.getDueDate(), invoice.getIssuedAt(), invoice.getPaidAt());
        return invoice;
    }

    @Override
    public void deleteByBooking(UUID bookingId) {
        if (bookingId == null) {
            return;
        }
        // Q: xoa ca partition (toan bo hoa don cua don dat phong)
        Cql.exec(session, "DELETE FROM invoices_by_booking WHERE booking_id = ?", bookingId);
    }

    @Override
    public long count() {
        Row row = Cql.exec(session, "SELECT COUNT(*) FROM invoices_by_booking").one();
        return row == null ? 0 : row.getLong(0);
    }

    private static Invoice toInvoice(Row row) {
        Invoice invoice = new Invoice();
        invoice.setKey(new InvoiceKey(row.getUuid("booking_id"), row.getUuid("invoice_id")));
        invoice.setGuestId(row.getString("guest_id"));
        invoice.setGuestName(row.getString("guest_name"));
        invoice.setHotelId(row.getString("hotel_id"));
        invoice.setRoomCharge(row.getBigDecimal("room_charge"));
        invoice.setServiceCharge(row.getBigDecimal("service_charge"));
        invoice.setTax(row.getBigDecimal("tax"));
        invoice.setTotalAmount(row.getBigDecimal("total_amount"));
        String status = row.getString("payment_status");
        try {
            invoice.setPaymentStatus(status == null ? PaymentStatus.UNPAID : PaymentStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            invoice.setPaymentStatus(PaymentStatus.UNPAID);
        }
        String method = row.getString("payment_method");
        if (method != null) {
            try {
                invoice.setPaymentMethod(PaymentMethod.valueOf(method));
            } catch (IllegalArgumentException ignored) {
                // giu null khi gia tri la
            }
        }
        invoice.setDueDate(row.getLocalDate("due_date"));
        invoice.setIssuedAt(row.getInstant("issued_at"));
        invoice.setPaidAt(row.getInstant("paid_at"));
        return invoice;
    }
}
