package com.example.leavemanagement.controller;

import com.example.leavemanagement.model.User;
import com.example.leavemanagement.service.LeaveService;
import com.example.leavemanagement.util.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/manager")
public class ManagerController {
    private final LeaveService leaveService;

    public ManagerController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping("/approvals")
    public String approvals(Model model) {
        User me = leaveService.findUserByUsername(SecurityUtils.currentUsername()).orElseThrow();
        model.addAttribute("pending", leaveService.pendingForManager(me));
        return "approvals";
    }

    @GetMapping("/dashboard")
    public String managerDashboard(Model model) {
        User me = leaveService.findUserByUsername(SecurityUtils.currentUsername()).orElseThrow();
        model.addAttribute("me", me);
        model.addAttribute("pendingCount", leaveService.pendingForManager(me).size());
        return "manager-dashboard";
    }

    @PostMapping("/approve/{id}")
    public String approve(@PathVariable Long id, @RequestParam(required = false) String comment) {
        User me = leaveService.findUserByUsername(SecurityUtils.currentUsername()).orElseThrow();
        leaveService.approve(id, me, comment);
        return "redirect:/manager/approvals?approved";
    }

    @PostMapping("/reject/{id}")
    public String reject(@PathVariable Long id, @RequestParam(required = false) String comment) {
        User me = leaveService.findUserByUsername(SecurityUtils.currentUsername()).orElseThrow();
        leaveService.reject(id, me, comment);
        return "redirect:/manager/approvals?rejected";
    }
}



