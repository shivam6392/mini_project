package com.example.leavemanagement.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class RoleBasedAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String roleParam = request.getParameter("role");
        Set<String> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        String redirect = "/dashboard"; // fallback
        if ("EMPLOYEE".equalsIgnoreCase(roleParam) && roles.contains("EMPLOYEE")) {
            redirect = "/employee/dashboard";
        } else if ("MANAGER".equalsIgnoreCase(roleParam) && (roles.contains("MANAGER") || roles.contains("HR") || roles.contains("ADMIN"))) {
            redirect = "/manager/approvals";
        } else if ("HR".equalsIgnoreCase(roleParam) && (roles.contains("HR") || roles.contains("ADMIN"))) {
            redirect = "/hr/overview";
        } else {
            // derive from authorities when no/invalid role param
            if (roles.contains("ADMIN") || roles.contains("HR")) {
                redirect = "/hr/overview";
            } else if (roles.contains("MANAGER")) {
                redirect = "/manager/approvals";
            } else if (roles.contains("EMPLOYEE")) {
                redirect = "/employee/dashboard";
            }
        }
        response.sendRedirect(request.getContextPath() + redirect);
    }
}


