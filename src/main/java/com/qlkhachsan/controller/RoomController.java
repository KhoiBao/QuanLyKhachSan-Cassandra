package com.qlkhachsan.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.qlkhachsan.model.Room;
import com.qlkhachsan.model.RoomKey;
import com.qlkhachsan.model.RoomStatus;
import com.qlkhachsan.repository.HotelStore;
import com.qlkhachsan.service.RoomService;
import com.qlkhachsan.service.ValidationException;
import com.qlkhachsan.util.Pages;

/**
 * Quan ly phong: loc theo khach san/loai/trang thai + them/sua/xoa.
 * Ma phong trong URL dang "KS001-101" (hotelId-roomNumber).
 */
@Controller
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;
    private final HotelStore hotelStore;

    @Value("${app.business.default-page-size:10}")
    private int pageSize;

    public RoomController(RoomService roomService, HotelStore hotelStore) {
        this.roomService = roomService;
        this.hotelStore = hotelStore;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String hotelId,
                       @RequestParam(required = false) String roomType,
                       @RequestParam(required = false) RoomStatus status,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) String edit,
                       Model model) {
        List<Room> rooms = roomService.search(hotelId, roomType, status);
        model.addAttribute("rooms", rooms);
        int totalPages = Pages.totalPages(rooms.size(), pageSize);
        int current = Pages.normalize(page, totalPages);
        model.addAttribute("pageItems", Pages.slice(rooms, current, pageSize));
        model.addAttribute("currentPage", current);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("queryString", Pages.queryString("hotelId", hotelId,
                "roomType", roomType, "status", status == null ? null : status.name()));
        model.addAttribute("hotels", hotelStore.findAll());
        model.addAttribute("roomTypes", RoomService.ROOM_TYPES);
        model.addAttribute("statuses", RoomStatus.values());
        model.addAttribute("fHotelId", hotelId);
        model.addAttribute("fRoomType", roomType);
        model.addAttribute("fStatus", status);

        Room editing = new Room();
        if (edit != null && edit.contains("-")) {
            try {
                int cut = edit.lastIndexOf('-');
                editing = roomService.getById(edit.substring(0, cut),
                        Integer.parseInt(edit.substring(cut + 1)));
            } catch (ValidationException | NumberFormatException ignored) {
                editing = new Room();
            }
        }
        model.addAttribute("editing", editing);
        return "rooms";
    }

    @PostMapping
    public String save(@RequestParam(required = false) String originalKey, Room form,
                       @RequestParam String price, RedirectAttributes ra) {
        boolean isNew = originalKey == null || originalKey.isBlank();
        try {
            form.setPricePerNight(parsePrice(price));
            if (isNew) {
                roomService.create(form);
                ra.addFlashAttribute("successMessage", "Đã thêm phòng " + form.getDisplayCode() + ".");
            } else {
                RoomKey key = parseKey(originalKey);
                roomService.update(key.getHotelId(), key.getRoomNumber(), form);
                ra.addFlashAttribute("successMessage", "Đã cập nhật phòng.");
            }
        } catch (ValidationException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        } catch (NumberFormatException e) {
            ra.addFlashAttribute("errorMessage", "Giá phòng phải là số.");
        }
        return "redirect:/rooms";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam String hotelId, @RequestParam Integer roomNumber,
                         RedirectAttributes ra) {
        try {
            roomService.delete(hotelId, roomNumber);
            ra.addFlashAttribute("successMessage", "Đã xóa phòng " + hotelId + "-" + roomNumber + ".");
        } catch (ValidationException | NumberFormatException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/rooms";
    }

    /* ================== TIEN ICH ================== */

    private static BigDecimal parsePrice(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Giá phòng không được để trống.");
        }
        return new BigDecimal(value.trim().replace(",", ""));
    }

    private static RoomKey parseKey(String composite) {
        int cut = composite.lastIndexOf('-');
        if (cut <= 0) {
            throw new ValidationException("Mã phòng không hợp lệ: " + composite);
        }
        try {
            return new RoomKey(composite.substring(0, cut), Integer.parseInt(composite.substring(cut + 1)));
        } catch (NumberFormatException e) {
            throw new ValidationException("Mã phòng không hợp lệ: " + composite);
        }
    }
}
