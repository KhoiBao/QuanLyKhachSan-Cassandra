package com.qlkhachsan.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.qlkhachsan.model.RoomStatus;
import com.qlkhachsan.service.DashboardService;

/**
 * Trang tong quan (dashboard): so lieu he thong + don dat phong gan nhat.
 * Cac trang con lai da tach thanh controller rieng (Hotel/Room/...).
 */
@Controller
public class HomeController {

    private final DashboardService dashboardService;

    public HomeController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("totalHotels", dashboardService.totalHotels());
        model.addAttribute("activeHotels", dashboardService.activeHotels());
        model.addAttribute("totalRooms", dashboardService.totalRooms());
        model.addAttribute("availableRooms", dashboardService.availableRooms());
        model.addAttribute("totalGuests", dashboardService.totalGuests());
        model.addAttribute("totalBookings", dashboardService.totalBookings());
        model.addAttribute("activeBookings", dashboardService.activeBookings());
        model.addAttribute("bookingsThisMonth", dashboardService.bookingsThisMonth());
        model.addAttribute("roomStatusCounts", dashboardService.roomStatusCounts());
        model.addAttribute("recentBookings", dashboardService.recentBookings(5));

        Map<RoomStatus, Long> counts = dashboardService.roomStatusCounts();
        model.addAttribute("countAvailable", counts.getOrDefault(RoomStatus.AVAILABLE, 0L));
        model.addAttribute("countOccupied", counts.getOrDefault(RoomStatus.OCCUPIED, 0L));
        model.addAttribute("countBooked", counts.getOrDefault(RoomStatus.BOOKED, 0L));
        model.addAttribute("countMaintenance", counts.getOrDefault(RoomStatus.MAINTENANCE, 0L));
        return "dashboard";
    }
}