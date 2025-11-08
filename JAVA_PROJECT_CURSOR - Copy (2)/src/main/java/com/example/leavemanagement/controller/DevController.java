package com.example.leavemanagement.controller;

import com.example.leavemanagement.model.*;
import com.example.leavemanagement.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/dev")
public class DevController {

    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PasswordEncoder passwordEncoder;

    public DevController(RoleRepository roleRepository,
                         DepartmentRepository departmentRepository,
                         UserRepository userRepository,
                         LeaveBalanceRepository leaveBalanceRepository,
                         PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/seed-basic")
    public Map<String, Object> seedBasic() {
        // Roles
        String[] roles = {"EMPLOYEE","MANAGER","HR","ADMIN"};
        for (String r : roles) {
            roleRepository.findByName(r).orElseGet(() -> {
                Role role = new Role(); role.setName(r); return roleRepository.save(role);
            });
        }
        // Departments
        Department eng = departmentRepository.findByName("Engineering").orElseGet(() -> { Department d=new Department(); d.setName("Engineering"); return departmentRepository.save(d); });
        Department hr = departmentRepository.findByName("HR").orElseGet(() -> { Department d=new Department(); d.setName("HR"); return departmentRepository.save(d); });

        ensureUser("admin","System Admin","admin@example.com", hr, new String[]{"ADMIN","HR"});
        ensureUser("manager1","Manager One","manager1@example.com", eng, new String[]{"MANAGER"});
        ensureUser("employee1","Employee One","employee1@example.com", eng, new String[]{"EMPLOYEE"});
        ensureUser("gourav","Gourav Sharma","gourav.sharma@example.com", eng, new String[]{"EMPLOYEE"});
        ensureUser("bunty","Bunty Singh","bunty.singh@example.com", eng, new String[]{"EMPLOYEE"});
        ensureUser("happy","Happy Sharma","happy.sharma@example.com", eng, new String[]{"EMPLOYEE"});
        ensureUser("akshat","Akshat Singh","akshat.singh@example.com", eng, new String[]{"EMPLOYEE"});
        ensureUser("mehak","Mehak Malik","mehak.malik@example.com", eng, new String[]{"EMPLOYEE"});

        long count = userRepository.count();
        return Map.of("status", "OK", "userCount", count);
    }

    private void ensureUser(String username, String fullName, String email, Department dept, String[] roleNames) {
        User u = userRepository.findByUsername(username).orElseGet(User::new);
        u.setUsername(username);
        u.setFullName(fullName);
        u.setEmail(email);
        u.setDepartment(dept);
        u.setPassword(passwordEncoder.encode(username.equals("admin")?"admin123":(username.equals("manager1")?"manager123":"employee123")));
        java.util.Set<Role> rs = new java.util.HashSet<>();
        for (String r : roleNames) {
            rs.add(roleRepository.findByName(r).get());
        }
        u.setRoles(rs);
        u = userRepository.save(u);
        // balances
        for (LeaveType t : LeaveType.values()) {
            if (leaveBalanceRepository.findByUserAndLeaveType(u, t).isEmpty()) {
                LeaveBalance b = new LeaveBalance();
                b.setUser(u);
                b.setLeaveType(t);
                b.setRemainingDays(10);
                leaveBalanceRepository.save(b);
            }
        }
    }
}


