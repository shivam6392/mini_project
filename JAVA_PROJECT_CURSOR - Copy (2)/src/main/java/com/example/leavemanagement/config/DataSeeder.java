package com.example.leavemanagement.config;

import com.example.leavemanagement.model.*;
import com.example.leavemanagement.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.EnumSet;
import java.util.Set;

@Configuration
public class DataSeeder {

    @Value("${app.default.leave.casual:12}")
    private int defaultCasual;

    @Value("${app.default.leave.sick:10}")
    private int defaultSick;

    @Value("${app.default.leave.earned:15}")
    private int defaultEarned;

    @Bean
    CommandLineRunner seed(
            RoleRepository roleRepository,
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            LeaveBalanceRepository leaveBalanceRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // Roles
            String[] roles = {"EMPLOYEE", "MANAGER", "HR", "ADMIN"};
            for (String r : roles) {
                roleRepository.findByName(r).orElseGet(() -> {
                    Role role = new Role();
                    role.setName(r);
                    return roleRepository.save(role);
                });
            }

            // Departments
            Department eng = departmentRepository.findByName("Engineering").orElseGet(() -> {
                Department d = new Department();
                d.setName("Engineering");
                return departmentRepository.save(d);
            });
            Department hr = departmentRepository.findByName("HR").orElseGet(() -> {
                Department d = new Department();
                d.setName("HR");
                return departmentRepository.save(d);
            });

            // Users
            // Ensure admin exists with known password
            User admin = userRepository.findByUsername("admin").orElseGet(() -> {
                User u = new User();
                u.setUsername("admin");
                u.setFullName("System Admin");
                u.setEmail("admin@example.com");
                u.setDepartment(hr);
                return u;
            });
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(Set.of(
                    roleRepository.findByName("ADMIN").get(),
                    roleRepository.findByName("HR").get()
            ));
            admin = userRepository.save(admin);
            createDefaultBalances(leaveBalanceRepository, admin);

            // Ensure manager exists with known password
            User manager = userRepository.findByUsername("manager1").orElseGet(() -> {
                User u = new User();
                u.setUsername("manager1");
                u.setFullName("Manager One");
                u.setEmail("manager1@example.com");
                u.setDepartment(eng);
                return u;
            });
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setRoles(Set.of(roleRepository.findByName("MANAGER").get()));
            manager = userRepository.save(manager);
            createDefaultBalances(leaveBalanceRepository, manager);

            // Ensure employee1 exists with known password
            User emp = userRepository.findByUsername("employee1").orElseGet(() -> {
                User u = new User();
                u.setUsername("employee1");
                u.setFullName("Employee One");
                u.setEmail("employee1@example.com");
                u.setDepartment(eng);
                return u;
            });
            emp.setPassword(passwordEncoder.encode("employee123"));
            emp.setRoles(Set.of(roleRepository.findByName("EMPLOYEE").get()));
            emp = userRepository.save(emp);
            createDefaultBalances(leaveBalanceRepository, emp);

            // Additional employees requested
            createEmployeeIfAbsent(
                    userRepository, roleRepository, leaveBalanceRepository, passwordEncoder,
                    "gourav", "Gourav Sharma", "gourav.sharma@example.com", eng
            );
            createEmployeeIfAbsent(
                    userRepository, roleRepository, leaveBalanceRepository, passwordEncoder,
                    "bunty", "Bunty Singh", "bunty.singh@example.com", eng
            );
            createEmployeeIfAbsent(
                    userRepository, roleRepository, leaveBalanceRepository, passwordEncoder,
                    "happy", "Happy Sharma", "happy.sharma@example.com", eng
            );
            createEmployeeIfAbsent(
                    userRepository, roleRepository, leaveBalanceRepository, passwordEncoder,
                    "akshat", "Akshat Singh", "akshat.singh@example.com", eng
            );
            createEmployeeIfAbsent(
                    userRepository, roleRepository, leaveBalanceRepository, passwordEncoder,
                    "mehak", "Mehak Malik", "mehak.malik@example.com", eng
            );
        };
    }

    private void createDefaultBalances(LeaveBalanceRepository repo, User user) {
        for (LeaveType t : EnumSet.allOf(LeaveType.class)) {
            LeaveBalance b = new LeaveBalance();
            b.setUser(user);
            b.setLeaveType(t);
            int days = switch (t) {
                case CASUAL -> defaultCasual;
                case SICK -> defaultSick;
                case EARNED -> defaultEarned;
            };
            b.setRemainingDays(days);
            repo.save(b);
        }
    }

    private void createEmployeeIfAbsent(
            UserRepository userRepository,
            RoleRepository roleRepository,
            LeaveBalanceRepository leaveBalanceRepository,
            PasswordEncoder passwordEncoder,
            String username,
            String fullName,
            String email,
            Department department
    ) {
        User user = userRepository.findByUsername(username).orElseGet(() -> {
            User u = new User();
            u.setUsername(username);
            u.setDepartment(department);
            return u;
        });
        user.setFullName(fullName);
        user.setEmail(email);
        user.setDepartment(department);
        user.setPassword(passwordEncoder.encode("employee123"));
        user.setRoles(Set.of(roleRepository.findByName("EMPLOYEE").get()));
        user = userRepository.save(user);
        createDefaultBalances(leaveBalanceRepository, user);
    }
}


