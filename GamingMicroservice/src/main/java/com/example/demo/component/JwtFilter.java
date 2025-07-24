package com.example.demo.component;

import com.example.demo.service.GamerService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final GamerService gamerService;
    private Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        logger.info("Получен токен: {}", token);

        try {
            Claims claims = jwtUtil.extractAllClaims(token);
            String subject = claims.getSubject();

            if (subject == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                return;
            }

            // 1. Service-токен (для /saveevas)
            if ("service-account".equals(subject)) {
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        subject, null, List.of(new SimpleGrantedAuthority("ROLE_SERVICE")));
                SecurityContextHolder.getContext().setAuthentication(auth);
                logger.info("Service-токен прошёл аутентификацию");
            }
            // 2. Пользовательский токен (для /game/process)
            else {
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = gamerService.loadUserByUsername(subject);

                    if (jwtUtil.validateToken(token, subject)) {
                        Authentication auth = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        logger.info("Пользователь {} аутентифицирован из JWT", subject);
                    }
                }
            }
        } catch (ExpiredJwtException e) {
            logger.warn("JWT токен просрочен");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
            return;
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Невалидный JWT токен: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }

        filterChain.doFilter(request, response);
    }



    /*@Override
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
    }*/
}
