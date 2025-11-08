package com.example.leavemanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/hr")
public class HrDashboardController {

    @GetMapping({"/dashboard", ""})
    public String hrLanding() {
        return "redirect:/hr/overview";
    }
}


