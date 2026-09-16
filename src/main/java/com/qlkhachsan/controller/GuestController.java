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

import com.qlkhachsan.model.Guest;
import com.qlkhachsan.service.GuestService;
import com.qlkhachsan.service.ValidationException;
import com.qlkhachsan.util.Pages;

/**
 * Quan ly khach hang (trang /customers): tim kiem + them/sua/xoa.
 */
@Controller
@RequestMapping("/customers")
public class GuestController {

    private final GuestService guestService;

    @Value("${app.business.default-page-size:10}")
    private int pageSize;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer page,
                       @RequestParam(required = false) String edit,
                       Model model) {
        List<Guest> guests = guestService.search(keyword);
        model.addAttribute("guests", guests);
        int totalPages = Pages.totalPages(guests.size(), pageSize);
        int current = Pages.normalize(page, totalPages);
        model.addAttribute("pageItems", Pages.slice(guests, current, pageSize));
        model.addAttribute("currentPage", current);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("queryString", Pages.queryString("keyword", keyword));
        model.addAttribute("fKeyword", keyword);
        Guest editing = new Guest();
        if (edit != null && !edit.isBlank()) {
            try {
                editing = guestService.getById(edit);
            } catch (ValidationException ignored) {
                editing = new Guest();
            }
        }
        model.addAttribute("editing", editing);
        return "customers";
    }

    @PostMapping
    public String save(@RequestParam(required = false) String guestId, Guest form,
                       RedirectAttributes ra) {
        boolean isNew = guestId == null || guestId.isBlank();
        try {
            if (isNew) {
                guestService.create(form);
                ra.addFlashAttribute("successMessage", "Đã thêm khách hàng.");
            } else {
                guestService.update(guestId, form);
                ra.addFlashAttribute("successMessage", "Đã cập nhật khách hàng " + guestId + ".");
            }
        } catch (ValidationException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/customers";
    }

    @PostMapping("/{guestId}/delete")
    public String delete(@PathVariable String guestId, RedirectAttributes ra) {
        try {
            guestService.delete(guestId);
            ra.addFlashAttribute("successMessage", "Đã xóa khách hàng " + guestId + ".");
        } catch (ValidationException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/customers";
    }
}
