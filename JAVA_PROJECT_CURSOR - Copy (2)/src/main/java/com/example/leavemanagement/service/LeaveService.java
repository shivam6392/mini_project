package com.example.leavemanagement.service;

import com.example.leavemanagement.model.*;
import com.example.leavemanagement.repository.LeaveBalanceRepository;
import com.example.leavemanagement.repository.LeaveRequestRepository;
import com.example.leavemanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final NotificationService notificationService;

    public LeaveService(LeaveRequestRepository leaveRequestRepository,
                        LeaveBalanceRepository leaveBalanceRepository,
                        UserRepository userRepository,
                        StorageService storageService,
                        NotificationService notificationService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.notificationService = notificationService;
    }

    public List<LeaveRequest> myLeaves(User employee) {
        return leaveRequestRepository.findByEmployee(employee);
    }

    public List<LeaveBalance> balances(User employee) {
        return leaveBalanceRepository.findByUser(employee);
    }

    public List<LeaveRequest> pendingForManager(User manager) {
        return leaveRequestRepository.findByManagerAndStatus(manager, LeaveStatus.PENDING);
    }

    public List<LeaveRequest> allApproved() {
        return leaveRequestRepository.findByStatus(LeaveStatus.APPROVED);
    }

    @Transactional
    public LeaveRequest apply(User employee,
                              User manager,
                              LeaveType type,
                              java.time.LocalDate start,
                              java.time.LocalDate end,
                              String reason,
                              MultipartFile document) throws IOException {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        int days = (int) ChronoUnit.DAYS.between(start, end) + 1;

        // overlap check
        List<LeaveRequest> overlapping = leaveRequestRepository
                .findByEmployeeAndStartDateLessThanEqualAndEndDateGreaterThanEqual(employee, end, start);
        boolean hasOverlap = overlapping.stream().anyMatch(lr -> lr.getStatus() != LeaveStatus.REJECTED);
        if (hasOverlap) {
            throw new IllegalStateException("Overlapping leave request exists");
        }

        LeaveBalance balance = leaveBalanceRepository.findByUserAndLeaveType(employee, type)
                .orElseThrow(() -> new IllegalStateException("No balance for leave type"));
        if (balance.getRemainingDays() < days) {
            throw new IllegalStateException("Insufficient leave balance");
        }

        LeaveRequest req = new LeaveRequest();
        req.setEmployee(employee);
        req.setManager(manager);
        req.setLeaveType(type);
        req.setStartDate(start);
        req.setEndDate(end);
        req.setTotalDays(days);
        req.setReason(reason);
        if (document != null && !document.isEmpty()) {
            req.setDocumentPath(storageService.store(document));
        }
        LeaveRequest saved = leaveRequestRepository.save(req);

        if (manager != null && manager.getEmail() != null) {
            notificationService.sendEmail(
                    manager.getEmail(),
                    "Leave approval required",
                    employee.getFullName() + " requested " + days + " day(s) of " + type + "."
            );
        }
        return saved;
    }

    @Transactional
    public LeaveRequest approve(Long requestId, User manager, String comment) {
        LeaveRequest req = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
        if (req.getManager() != null && !req.getManager().getId().equals(manager.getId())) {
            throw new SecurityException("Not authorized to approve this request");
        }
        if (req.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Request already processed");
        }

        LeaveBalance balance = leaveBalanceRepository.findByUserAndLeaveType(req.getEmployee(), req.getLeaveType())
                .orElseThrow();
        if (balance.getRemainingDays() < req.getTotalDays()) {
            throw new IllegalStateException("Insufficient balance at approval time");
        }
        balance.setRemainingDays(balance.getRemainingDays() - req.getTotalDays());
        leaveBalanceRepository.save(balance);

        req.setStatus(LeaveStatus.APPROVED);
        req.setManagerComment(comment);
        LeaveRequest saved = leaveRequestRepository.save(req);

        if (req.getEmployee().getEmail() != null) {
            notificationService.sendEmail(
                    req.getEmployee().getEmail(),
                    "Leave Approved",
                    "Your leave request (" + req.getLeaveType() + ") was approved."
            );
        }
        return saved;
    }

    @Transactional
    public LeaveRequest reject(Long requestId, User manager, String comment) {
        LeaveRequest req = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
        if (req.getManager() != null && !req.getManager().getId().equals(manager.getId())) {
            throw new SecurityException("Not authorized to reject this request");
        }
        if (req.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalStateException("Request already processed");
        }
        req.setStatus(LeaveStatus.REJECTED);
        req.setManagerComment(comment);
        LeaveRequest saved = leaveRequestRepository.save(req);
        if (req.getEmployee().getEmail() != null) {
            notificationService.sendEmail(
                    req.getEmployee().getEmail(),
                    "Leave Rejected",
                    "Your leave request (" + req.getLeaveType() + ") was rejected."
            );
        }
        return saved;
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}



