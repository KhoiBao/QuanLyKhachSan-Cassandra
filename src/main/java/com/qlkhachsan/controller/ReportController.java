package com.qlkhachsan.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.qlkhachsan.service.ReportService;

/**
 * Bao cao doanh thu: theo khach san + theo thang (6 thang gan nhat).
 * Chi ADMIN va MANAGER duoc truy cap (cau hinh o SecurityConfig).
 */
@Controller
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        List<ReportService.MonthRevenue> months = reportService.revenueByMonth(6);

        BigDecimal maxRevenue = BigDecimal.ZERO;
        for (ReportService.MonthRevenue month : months) {
            if (month.revenue().compareTo(maxRevenue) > 0) {
                maxRevenue = month.revenue();
            }
        }

        model.addAttribute("revenueByHotel", reportService.revenueByHotel());
        model.addAttribute("revenueByMonth", months);
        model.addAttribute("totalRevenue", reportService.totalRevenue());
        model.addAttribute("revenueToday", reportService.revenueToday());
        model.addAttribute("revenueThisMonth", reportService.revenueThisMonth());
        model.addAttribute("paidInvoiceCount", reportService.paidInvoiceCount());
        model.addAttribute("maxRevenue", maxRevenue);
        return "reports";
    }
}
