package com.example.vinyltrackerapi.api.filters;

import com.example.vinyltrackerapi.service.VisitCounterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class VisitCounterFilter extends OncePerRequestFilter {
    private final VisitCounterService visitCounterService;

    public VisitCounterFilter(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        visitCounterService.increment(uri);

        filterChain.doFilter(request, response);
    }
}