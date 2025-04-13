package com.example.vinyltrackerapi.api.filters;

import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.utils.JwtUtil;
import com.example.vinyltrackerapi.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        final String email = jwtUtil.extractUsername(jwt);
        final String role = jwtUtil.extractRole(jwt); // <--- это важно

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<User> userOptional = userService.getUserByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();

                if (jwtUtil.isTokenValid(jwt, new org.springframework.security.core.userdetails.User(
                        user.getEmail(), user.getPassword(), List.of()))) {
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    var authToken = new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            null,
                            authorities
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}