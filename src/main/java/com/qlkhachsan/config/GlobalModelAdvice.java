package com.qlkhachsan.config;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.qlkhachsan.repository.UserStore;

/**
 * Du lieu dung chung moi trang: ngay hom nay + thong tin nguoi dang nhap
 * (sidebar, phan quyen hien thi nut them/xoa).
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final UserStore userStore;

    public GlobalModelAdvice(UserStore userStore) {
        this.userStore = userStore;
    }

    @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("todayText", LocalDate.now(ZONE)
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            return; // chua dang nhap (trang login) hoac anonymous
        }
        userStore.findByUsername(auth.getName()).ifPresent(user -> {
            String fullName = user.getFullName() == null || user.getFullName().isBlank()
                    ? user.getUsername() : user.getFullName();
            model.addAttribute("loginUsername", user.getUsername());
            model.addAttribute("loginFullName", fullName);
            model.addAttribute("loginRoleLabel", user.getRole().getLabel());
            model.addAttribute("loginInitial", fullName.substring(0, 1).toUpperCase());
            model.addAttribute("canDelete", user.getRole().canDelete());
            model.addAttribute("canManageHotels", user.getRole().canManageHotels());
            model.addAttribute("canViewReports", user.getRole().canViewReports());
            model.addAttribute("canRefund", user.getRole().canRefund());
        });
    }
}
