package com.medicore.api.config;

import com.medicore.api.util.ActivityLogger;
import com.medicore.api.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class ClinicalAuditInterceptor implements HandlerInterceptor {

    @Autowired
    private ActivityLogger activityLogger;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Only log API calls, skip static assets or auth itself to avoid loops
        if (path.startsWith("/api") && !path.contains("/auth") && !path.contains("/activities")) {
            String authHeader = request.getHeader("Authorization");
            String username = "System";

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    username = jwtUtil.extractUsername(token);
                } catch (Exception e) {
                    // Token might be invalid or expired, fallback to System
                }
            }

            // Map common API paths to human-readable actions
            String description = translatePathToAction(method, path, username);
            if (description != null) {
                activityLogger.log("ActivityIcon", description, username);
            }
        }
        return true;
    }

    private String translatePathToAction(String method, String path, String user) {
        if (method.equals("GET")) {
            if (path.endsWith("/patients")) return user + " accessed Patient Registry";
            if (path.endsWith("/appointments")) return user + " viewed Clinical Schedule";
            if (path.endsWith("/wards")) return user + " checked Ward Occupancy";
            if (path.endsWith("/vitals")) return user + " reviewed Patient Vitals";
            if (path.endsWith("/lab-requests") || path.contains("/labtech/queue")) return user + " accessed Lab Diagnostic Queue";
            if (path.endsWith("/prescriptions")) return user + " reviewed Pharmacological Protocols";
            if (path.endsWith("/billing/invoices")) return user + " accessed Financial Records";
            if (path.endsWith("/users")) return user + " accessed User Management";
            if (path.contains("/dashboard")) return user + " viewed Operational Dashboard";
        } else if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH")) {
            return user + " initiated a " + method + " operation on " + path;
        } else if (method.equals("DELETE")) {
            return user + " deleted a record from " + path;
        }
        return null; // Don't log everything to avoid noise
    }
}
