package com.pizzaria.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class IpUtils {

    private IpUtils() {}

    public static String getClientIP() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String xfwd = request.getHeader("X-Forwarded-For");
            if (xfwd != null && !xfwd.isBlank()) {
                return xfwd.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        return "unknown";
    }
}
