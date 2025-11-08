package com.example.leavemanagement.controller;

import com.example.leavemanagement.model.LeaveRequest;
import com.example.leavemanagement.service.ReportService;
import com.example.leavemanagement.service.ReportService.LeaveEntry;
import com.example.leavemanagement.service.ReportService.ListWrapper;
import com.example.leavemanagement.service.CalendarService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final CalendarService calendarService;
    private final ReportService reportService;

    public ReportController(CalendarService calendarService, ReportService reportService) {
        this.calendarService = calendarService;
        this.reportService = reportService;
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel() {
        List<LeaveRequest> approved = calendarService.approvedLeaves();
        byte[] data = reportService.exportExcel(approved);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=leaves.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }

    @GetMapping("/xml")
    public ResponseEntity<byte[]> xml() {
        List<LeaveRequest> approved = calendarService.approvedLeaves();
        List<LeaveEntry> entries = approved.stream().map(lr -> {
            LeaveEntry e = new LeaveEntry();
            e.employee = lr.getEmployee().getFullName();
            e.type = lr.getLeaveType().name();
            e.status = lr.getStatus().name();
            e.startDate = lr.getStartDate().toString();
            e.endDate = lr.getEndDate().toString();
            e.days = lr.getTotalDays();
            return e;
        }).collect(Collectors.toList());
        ListWrapper wrapper = new ListWrapper();
        wrapper.items = entries;
        byte[] data = reportService.exportXml(wrapper);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=leaves.xml")
                .contentType(MediaType.APPLICATION_XML)
                .body(data);
    }
}



