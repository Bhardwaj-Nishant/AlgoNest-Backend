package com.algonest.AlgoNest_Backend.security;

import com.algonest.AlgoNest_Backend.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.isTokenValid(token)) {
                // 1. Extract user details from JWT
                Claims claims = jwtUtil.extractAllClaims(token);
                UUID authUserId = UUID.fromString(claims.getSubject());
                String email = claims.get("email", String.class);

                // 2. Extract displayName from user_metadata
                String displayName = null;
                Object metadata = claims.get("user_metadata");
                if (metadata instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> meta = (java.util.Map<String, Object>) metadata;
                    displayName = (String) meta.get("name");
                }
                // Fallback to email prefix if no name is found
                if (displayName == null || displayName.isEmpty()) {
                    displayName = email.split("@")[0];
                }

                // 3. ✅ AUTO-CREATE OR UPDATE USER IN LOCAL DATABASE
                userService.findOrCreateUser(authUserId, email, displayName);

                // 4. Set authentication in SecurityContext
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(authUserId, null, Collections.emptyList());
                authentication.setDetails(email);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }
}