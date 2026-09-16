package com.qlkhachsan.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.qlkhachsan.model.Invoice;
import com.qlkhachsan.model.PaymentMethod;
import com.qlkhachsan.model.PaymentStatus;
import com.qlkhachsan.repository.BookingStore;
import com.qlkhachsan.service.InvoiceService;
import com.qlkhachsan.service.ValidationException;
import com.qlkhachsan.util.Pages;

/**
 * Quan ly hoa don: danh sach + tao hoa don tu don dat phong +
 * thanh toan / hoan tien.
 */
@Controller
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final BookingStore bookingStore;

    @Value("${app.business.default-page-size:10}")
    private int pageSize;

    public InvoiceController(InvoiceService invoiceService, BookingStore bookingStore) {
        this.invoiceService = invoiceService;
        this.bookingStore = bookingStore;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) PaymentStatus status,
                       @RequestParam(required = false) Integer page,
                       Model model) {
        List<Invoice> invoices = invoiceService.search(keyword, status);
        model.addAttribute("invoices", invoices);
        int totalPages = Pages.totalPages(invoices.size(), pageSize);
        int current = Pages.normalize(page, totalPages);
        model.addAttribute("pageItems", Pages.slice(invoices, current, pageSize));
        model.addAttribute("currentPage", current);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("queryString", Pages.queryString("keyword", keyword,
                "status", status == null ? null : status.name()));
        model.addAttribute("statuses", PaymentStatus.values());
        model.addAttribute("methods", PaymentMethod.values());
        model.addAttribute("fKeyword", keyword);
        model.addAttribute("fStatus", status);
        model.addAttribute("bookings", bookingStore.findAll());
        return "invoices";
    }

    /** Tao hoa don cho mot don dat phong (thuong dung sau khi tra phong). */
    @PostMapping("/create")
    public String create(@RequestParam String bookingId, RedirectAttributes ra) {
        try {
            invoiceService.createForBookingById(UUID.fromString(bookingId));
            ra.addFlashAttribute("successMessage", "Đã tạo hóa đơn cho đơn đặt phòng.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", "Mã đơn không hợp lệ.");
        } catch (ValidationException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/invoices";
    }

    @PostMapping("/pay")
    public String pay(@RequestParam String bookingId, @RequestParam String invoiceId,
                      @RequestParam PaymentMethod method, RedirectAttributes ra) {
        try {
            invoiceService.pay(UUID.fromString(bookingId), UUID.fromString(invoiceId), method);
            ra.addFlashAttribute("successMessage", "Đã ghi nhận thanh toán.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", "Mã hóa đơn không hợp lệ.");
        } catch (ValidationException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/invoices";
    }

    @PostMapping("/refund")
    public String refund(@RequestParam String bookingId, @RequestParam String invoiceId,
                         RedirectAttributes ra) {
        try {
            invoiceService.refund(UUID.fromString(bookingId), UUID.fromString(invoiceId));
            ra.addFlashAttribute("successMessage", "Đã hoàn tiền hóa đơn.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", "Mã hóa đơn không hợp lệ.");
        } catch (ValidationException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/invoices";
    }
}
