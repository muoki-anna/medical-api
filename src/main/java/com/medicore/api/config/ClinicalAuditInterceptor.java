package com.medicore.api.config;

import com.medicore.api.util.ActivityLogger;
import com.medicore.api.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

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
            String displayName = "System";

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    displayName = jwtUtil.extractClaim(token, claims -> (String) claims.get("name"));
                    if (displayName == null) displayName = jwtUtil.extractUsername(token);
                } catch (Exception e) {
                    // Token might be invalid or expired, fallback to System
                }
            }

            // Map common API paths to human-readable actions
            Object[] result = translatePathToAction(method, path, displayName);
            if (result != null) {
                activityLogger.log((String)result[0], (String)result[1], displayName);
            }
        }
        return true;
    }

    private Object[] translatePathToAction(String method, String path, String user) {
        if (method.equals("GET")) {
            if (path.endsWith("/patients") || path.contains("/patient/")) 
                return new Object[]{"PersonIcon", user + " accessed Patient Registry"};
            
            if (path.endsWith("/appointments")) 
                return new Object[]{"CalendarIcon", user + " viewed Clinical Schedule"};
            
            if (path.endsWith("/wards")) 
                return new Object[]{"BedIcon", user + " checked Ward Occupancy"};
            
            if (path.endsWith("/vitals")) 
                return new Object[]{"ActivityIcon", user + " reviewed Patient Vitals"};
            
            if (path.endsWith("/lab-requests") || path.contains("/labtech/")) 
                return new Object[]{"FlaskIcon", user + " accessed Lab Diagnostic Queue"};
            
            if (path.endsWith("/prescriptions")) 
                return new Object[]{"PillIcon", user + " reviewed Pharmacological Protocols"};
            
            if (path.contains("/billing")) 
                return new Object[]{"DollarIcon", user + " accessed Financial Records"};
            
            if (path.endsWith("/users") || path.contains("/staff")) 
                return new Object[]{"PersonIcon", user + " accessed Human Resources/Staff Management"};
            
            if (path.contains("/dashboard")) 
                return new Object[]{"MonitorIcon", user + " viewed Operational Dashboard"};
            
            if (path.contains("/inventory")) 
                return new Object[]{"PackageIcon", user + " checked Medical Inventory"};
            
            if (path.contains("/reports")) 
                return new Object[]{"ReportIcon", user + " generated System Reports"};
            
            if (path.contains("/nurse-tasks")) 
                return new Object[]{"QueueIcon", user + " reviewed Nursing Task List"};
            
            if (path.contains("/settings")) 
                return new Object[]{"SettingsIcon", user + " accessed System Settings"};
            
        } else if (method.equals("POST") || method.equals("PUT") || method.equals("PATCH")) {
            String resource = path.substring(path.lastIndexOf("/") + 1);
            return new Object[]{"EditIcon", user + " updated " + resource + " record"};
        } else if (method.equals("DELETE")) {
            String resource = path.substring(path.lastIndexOf("/") + 1);
            return new Object[]{"TrashIcon", user + " removed " + resource + " from system"};
        }
        return null;
    }
}
