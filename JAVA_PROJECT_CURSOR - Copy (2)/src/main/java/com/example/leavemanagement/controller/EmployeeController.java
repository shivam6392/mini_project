package com.example.leavemanagement.controller;

import com.example.leavemanagement.model.LeaveType;
import com.example.leavemanagement.model.User;
import com.example.leavemanagement.service.LeaveService;
import com.example.leavemanagement.util.SecurityUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    private final LeaveService leaveService;

    public EmployeeController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping("/apply")
    public String applyForm(Model model) {
        model.addAttribute("types", LeaveType.values());
        return "apply";
    }

    @PostMapping("/apply")
    public String applySubmit(@RequestParam("type") LeaveType type,
                              @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                              @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                              @RequestParam("reason") String reason,
                              @RequestParam(value = "doc", required = false) MultipartFile doc,
                              Model model) {
        try {
            String username = SecurityUtils.currentUsername();
            User me = leaveService.findUserByUsername(username).orElseThrow();
            // naive: choose a manager if any user has role MANAGER; for demo use manager1
            User manager = leaveService.findUserByUsername("manager1").orElse(null);
            leaveService.apply(me, manager, type, start, end, reason, doc);
            return "redirect:/employee/dashboard?applied";
        } catch (Exception e) {
            model.addAttribute("types", LeaveType.values());
            model.addAttribute("error", e.getMessage());
            return "apply";
        }
    }

    @GetMapping("/dashboard")
    public String employeeDashboard(Model model) {
        String username = SecurityUtils.currentUsername();
        User me = leaveService.findUserByUsername(username).orElseThrow();
        model.addAttribute("me", me);
        model.addAttribute("balances", leaveService.balances(me));
        model.addAttribute("myLeaves", leaveService.myLeaves(me));
        return "dashboard";
    }
}



