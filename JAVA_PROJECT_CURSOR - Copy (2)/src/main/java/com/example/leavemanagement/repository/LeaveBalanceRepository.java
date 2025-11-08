package com.example.leavemanagement.repository;

import com.example.leavemanagement.model.LeaveBalance;
import com.example.leavemanagement.model.LeaveType;
import com.example.leavemanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    List<LeaveBalance> findByUser(User user);
    Optional<LeaveBalance> findByUserAndLeaveType(User user, LeaveType leaveType);
}



