package com.example.leavemanagement.controller;

import com.example.leavemanagement.model.User;
import com.example.leavemanagement.service.LeaveService;
import com.example.leavemanagement.util.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final LeaveService leaveService;

    public DashboardController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        String username = SecurityUtils.currentUsername();
        User me = leaveService.findUserByUsername(username).orElseThrow();
        model.addAttribute("me", me);
        model.addAttribute("balances", leaveService.balances(me));
        model.addAttribute("myLeaves", leaveService.myLeaves(me));
        return "dashboard";
    }
}



