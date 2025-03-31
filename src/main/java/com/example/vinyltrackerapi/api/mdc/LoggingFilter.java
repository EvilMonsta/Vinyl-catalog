package com.example.vinyltrackerapi.api.mdc;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class LoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String date = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        MDC.put("timestamp", date);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}