package com.example.leavemanagement.controller;

import com.example.leavemanagement.model.LeaveRequest;
import com.example.leavemanagement.service.CalendarService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping
    public String calendarView(Model model) {
        return "calendar";
    }

    @GetMapping(value = "/events", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Map<String, Object>> events() {
        List<LeaveRequest> approved = calendarService.approvedLeaves();
        return approved.stream().map(lr -> {
            Map<String, Object> event = new HashMap<>();
            event.put("title", lr.getEmployee().getFullName() + " (" + lr.getLeaveType().name() + ")");
            event.put("start", lr.getStartDate().toString());
            event.put("end", lr.getEndDate().plusDays(1).toString());
            return event;
        }).collect(Collectors.toList());
    }
}


