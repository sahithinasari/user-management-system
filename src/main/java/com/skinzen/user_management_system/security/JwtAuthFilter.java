package com.skinzen.user_management_system.security;

import com.skinzen.user_management_system.enums.UserStatus;
import com.skinzen.user_management_system.model.User;
import com.skinzen.user_management_system.repository.UserRepository;
import com.skinzen.user_management_system.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        /*
         * No JWT provided.
         *
         * Let Spring Security decide whether the endpoint
         * requires authentication.
         */
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);

            /*
             * Extract email from JWT.
             */
            String email = jwtUtil.extractSubject(token);

            if (email == null ||
                    SecurityContextHolder.getContext().getAuthentication() != null) {

                chain.doFilter(request, response);
                return;
            }

            /*
             * Load the CURRENT user from the database.
             *
             * This is important because the user's status or role
             * may have changed after the JWT was issued.
             */
            User user = userRepository.findByEmail(email)
                    .orElse(null);

            if (user == null) {
                log.warn("JWT belongs to a non-existent user");
                chain.doFilter(request, response);
                return;
            }

            /*
             * User must currently be ACTIVE.
             *
             * This prevents users with LOCKED, DISABLED or
             * PENDING_VERIFICATION status from using an existing JWT.
             */
            if (user.getStatus()!=UserStatus.ACTIVE) {
                log.warn(
                        "Authentication rejected for inactive user: {}",
                        email
                );

                chain.doFilter(request, response);
                return;
            }

            /*
             * Validate JWT signature and expiration.
             */
            if (!jwtUtil.validateToken(token, user.getEmail())) {
                log.warn("Invalid JWT for user: {}", email);

                chain.doFilter(request, response);
                return;
            }

            /*
             * Create our custom authenticated principal.
             */
            CustomUserPrincipal principal =
                    new CustomUserPrincipal(user);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

        } catch (Exception ex) {

            /*
             * Never expose JWT parsing/validation details to the client.
             *
             * The request remains unauthenticated and Spring Security's
             * AuthenticationEntryPoint will return the 401 response.
             */
            log.warn(
                    "JWT authentication failed: {}",
                    ex.getMessage()
            );
        }

        chain.doFilter(request, response);
    }
}
