package com.qlkhachsan.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.qlkhachsan.model.Hotel;
import com.qlkhachsan.model.HotelStatus;
import com.qlkhachsan.service.HotelService;
import com.qlkhachsan.service.ValidationException;
import com.qlkhachsan.util.Pages;

/**
 * Quan ly khach san: danh sach + tim kiem/loc + them/sua/xoa.
 */
@Controller
@RequestMapping("/hotels")
public class HotelController {

    private final HotelService hotelService;

    /** So dong moi trang (application.properties: app.business.default-page-size). */
    @Value("${app.business.default-page-size:10}")
    private int pageSize;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String city,
                       @RequestParam(required = false) HotelStatus status,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) String edit,
                       Model model) {
        List<Hotel> hotels = hotelService.search(keyword, city, status);
        model.addAttribute("hotels", hotels);
        addPage(model, page, hotels,
                Pages.queryString("keyword", keyword, "city", city,
                        "status", status == null ? null : status.name()));
        model.addAttribute("cities", hotelService.cities());
        model.addAttribute("statuses", HotelStatus.values());
        model.addAttribute("fKeyword", keyword);
        model.addAttribute("fCity", city);
        model.addAttribute("fStatus", status);
        if (edit != null && !edit.isBlank()) {
            try {
                model.addAttribute("editing", hotelService.getById(edit));
            } catch (ValidationException ignored) {
                // ma khong ton tai: hien form them moi
            }
        }
        if (!model.containsAttribute("editing")) {
            model.addAttribute("editing", new Hotel());
        }
        return "hotels";
    }

    /** Do so lieu phan trang vao model (pageItems / currentPage / totalPages / queryString). */
    private void addPage(Model model, Integer page, List<?> items, String query) {
        int totalPages = Pages.totalPages(items.size(), pageSize);
        int current = Pages.normalize(page, totalPages);
        model.addAttribute("pageItems", Pages.slice(items, current, pageSize));
        model.addAttribute("currentPage", current);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("queryString", query);
    }

    @PostMapping
    public String save(@RequestParam(required = false) String hotelId, Hotel form,
                       RedirectAttributes ra) {
        boolean isNew = hotelId == null || hotelId.isBlank();
        try {
            if (isNew) {
                hotelService.create(form);
                ra.addFlashAttribute("successMessage", "Đã thêm khách sạn mới.");
            } else {
                hotelService.update(hotelId, form);
                ra.addFlashAttribute("successMessage", "Đã cập nhật khách sạn " + hotelId + ".");
            }
        } catch (ValidationException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/hotels";
    }

    @PostMapping("/{hotelId}/delete")
    public String delete(@PathVariable String hotelId, RedirectAttributes ra) {
        try {
            hotelService.delete(hotelId);
            ra.addFlashAttribute("successMessage", "Đã xóa khách sạn " + hotelId + ".");
        } catch (ValidationException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/hotels";
    }
}
