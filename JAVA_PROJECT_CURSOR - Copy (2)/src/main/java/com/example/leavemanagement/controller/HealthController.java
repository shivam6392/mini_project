package com.example.leavemanagement.controller;

import com.example.leavemanagement.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    private final UserRepository userRepository;

    public HealthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();
        try {
            long userCount = userRepository.count();
            status.put("status", "UP");
            status.put("database", "Connected");
            status.put("userCount", userCount);
        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("database", "Disconnected");
            status.put("error", e.getMessage());
        }
        return status;
    }
}

