package com.example.leavemanagement.controller;

import com.example.leavemanagement.model.LeaveRequest;
import com.example.leavemanagement.service.CalendarService;
import com.example.leavemanagement.service.LeaveService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/hr")
public class HrController {

    private final LeaveService leaveService;
    private final CalendarService calendarService;

    public HrController(LeaveService leaveService, CalendarService calendarService) {
        this.leaveService = leaveService;
        this.calendarService = calendarService;
    }

    @GetMapping("/overview")
    public String overview(Model model) {
        List<LeaveRequest> approved = calendarService.approvedLeaves();
        model.addAttribute("approved", approved);
        return "hr-overview";
    }

    @GetMapping("/dashboard")
    public String hrDashboard(Model model) {
        List<LeaveRequest> approved = calendarService.approvedLeaves();
        model.addAttribute("approvedCount", approved.size());
        return "hr-dashboard";
    }
}



