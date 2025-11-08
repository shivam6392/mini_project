package com.example.leavemanagement.service;

import com.example.leavemanagement.model.LeaveRequest;
import com.example.leavemanagement.model.LeaveStatus;
import com.example.leavemanagement.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalendarService {
    private final LeaveRequestRepository leaveRequestRepository;

    public CalendarService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public List<LeaveRequest> approvedLeaves() {
        return leaveRequestRepository.findByStatus(LeaveStatus.APPROVED);
    }
}



