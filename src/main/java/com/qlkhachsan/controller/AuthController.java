package com.qlkhachsan.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.qlkhachsan.service.AccountService;
import com.qlkhachsan.service.ValidationException;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Trang dang nhap (Spring Security xu ly POST /login) va dang ky tai khoan moi.
 */
@Controller
public class AuthController {

    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String registered,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Sai tên đăng nhập hoặc mật khẩu.");
        }
        if (logout != null) {
            model.addAttribute("infoMessage", "Bạn đã đăng xuất khỏi hệ thống.");
        }
        if (registered != null) {
            model.addAttribute("infoMessage", "Đăng ký thành công, mời bạn đăng nhập.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("form") RegisterForm form,
                           BindingResult binding, Model model, RedirectAttributes ra) {
        if (binding.hasErrors()) {
            model.addAttribute("errorMessage", "Dữ liệu chưa hợp lệ, hãy kiểm tra lại.");
            return "register";
        }
        try {
            accountService.register(form.getUsername(), form.getPassword(),
                    form.getFullName(), form.getEmail());
        } catch (ValidationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
        ra.addAttribute("registered", "");
        return "redirect:/login";
    }

    /** Form dang ky tai khoan (mac dinh vai tro STAFF - le tan). */
    public static class RegisterForm {

        @NotBlank
        private String username;

        @NotBlank
        @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
        private String password;

        @NotBlank
        private String fullName;

        @NotBlank
        @Email(message = "Email không hợp lệ")
        private String email;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
