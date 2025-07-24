package com.example.demo.component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private Logger logger = LoggerFactory.getLogger(JwtFilter.class);


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            logger.info("Полученный токен: " + token);
            if (jwtUtil.validateServiceToken(token)) {
                String subject = jwtUtil.extractSubject(token);
                if ("service-account".equals(subject)) {
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            subject, null, new ArrayList<>());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    logger.info("Токен успешно проверен.");
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token subject");
                    return;
                }
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                logger.info("Невалидный токен.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
