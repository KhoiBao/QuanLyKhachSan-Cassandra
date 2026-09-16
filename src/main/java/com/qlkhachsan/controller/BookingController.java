package com.qlkhachsan.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.qlkhachsan.model.BookingById;
import com.qlkhachsan.model.BookingStatus;
import com.qlkhachsan.model.Room;
import com.qlkhachsan.repository.GuestStore;
import com.qlkhachsan.repository.HotelStore;
import com.qlkhachsan.service.BookingService;
import com.qlkhachsan.service.RoomService;
import com.qlkhachsan.service.ValidationException;
import com.qlkhachsan.util.Pages;

/**
 * Quan ly dat phong: loc/tra cuu + tao don + doi trang thai (nhan/tra phong)
 * + xoa don. Du lieu dropdown khach/phong lay tu cac Store.
 */
@Controller
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final GuestStore guestStore;
    private final HotelStore hotelStore;
    private final RoomService roomService;

    @Value("${app.business.default-page-size:10}")
    private int pageSize;

    public BookingController(BookingService bookingService, GuestStore guestStore,
                             HotelStore hotelStore, RoomService roomService) {
        this.bookingService = bookingService;
        this.guestStore = guestStore;
        this.hotelStore = hotelStore;
        this.roomService = roomService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String hotelId,
                       @RequestParam(required = false) BookingStatus status,
                       @RequestParam(required = false) String from,
                       @RequestParam(required = false) String to,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) String edit,
                       Model model) {
        LocalDate fromDate = parseDate(from);
        LocalDate toDate = parseDate(to);
        List<BookingById> bookings = bookingService.search(hotelId, status, fromDate, toDate, keyword);
        model.addAttribute("bookings", bookings);
        int totalPages = Pages.totalPages(bookings.size(), pageSize);
        int current = Pages.normalize(page, totalPages);
        model.addAttribute("pageItems", Pages.slice(bookings, current, pageSize));
        model.addAttribute("currentPage", current);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("queryString", Pages.queryString("hotelId", hotelId,
                "status", status == null ? null : status.name(),
                "from", from, "to", to, "keyword", keyword));
        model.addAttribute("hotels", hotelStore.findAll());
        model.addAttribute("guests", guestStore.findAll());
        model.addAttribute("roomsByHotel", buildRoomsByHotel());
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("fHotelId", hotelId);
        model.addAttribute("fStatus", status);
        model.addAttribute("fFrom", from);
        model.addAttribute("fTo", to);
        model.addAttribute("fKeyword", keyword);
        model.addAttribute("editing", edit == null || edit.isBlank()
                ? null : safeFind(edit));
        return "bookings";
    }

    /**
     * Nhom phong theo khach san (dung cho dropdown chon phong khi tao don):
     * { "KS001": [ {roomNumber, roomType, statusLabel, bookable}, ... ] }
     */
    private Map<String, List<Map<String, Object>>> buildRoomsByHotel() {
        Map<String, List<Map<String, Object>>> result = new TreeMap<>();
        for (Room room : roomService.search(null, null, null)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("roomNumber", room.getRoomNumber());
            item.put("roomType", room.getRoomType() == null ? "" : room.getRoomType());
            item.put("statusLabel", room.getStatus() == null ? "" : room.getStatus().getLabel());
            item.put("bookable", room.isAvailable());
            result.computeIfAbsent(room.getHotelId(), k -> new ArrayList<>()).add(item);
        }
        return result;
    }

    @PostMapping
    public String create(@RequestParam String guestId,
                         @RequestParam String hotelId,
                         @RequestParam Integer roomNumber,
                         @RequestParam String checkInDate,
                         @RequestParam String checkOutDate,
                         @RequestParam(required = false) String notes,
                         RedirectAttributes ra) {
        try {
            bookingService.create(guestId, hotelId, roomNumber,
                    parseDate(checkInDate), parseDate(checkOutDate), notes);
            ra.addFlashAttribute("successMessage", "Đã tạo đơn đặt phòng.");
        } catch (ValidationException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/bookings";
    }

    @PostMapping("/{bookingId}/status")
    public String changeStatus(@PathVariable String bookingId,
                               @RequestParam BookingStatus status,
                               RedirectAttributes ra) {
        try {
            bookingService.changeStatus(UUID.fromString(bookingId), status);
            ra.addFlashAttribute("successMessage", "Đã cập nhật trạng thái: " + status.getLabel() + ".");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", "Mã đơn không hợp lệ.");
        } catch (ValidationException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/bookings";
    }

    @PostMapping("/{bookingId}/delete")
    public String delete(@PathVariable String bookingId, RedirectAttributes ra) {
        try {
            bookingService.delete(UUID.fromString(bookingId));
            ra.addFlashAttribute("successMessage", "Đã xóa đơn đặt phòng.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", "Mã đơn không hợp lệ.");
        } catch (ValidationException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/bookings";
    }

    /* ================== TIEN ICH ================== */

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException("Ngày không hợp lệ: " + value);
        }
    }

    private Object safeFind(String id) {
        try {
            return bookingService.getById(UUID.fromString(id));
        } catch (IllegalArgumentException | ValidationException e) {
            return null;
        }
    }
}
