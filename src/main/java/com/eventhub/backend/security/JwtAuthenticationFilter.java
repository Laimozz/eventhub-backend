package com.eventhub.backend.security;

import com.eventhub.backend.exception.GlobalExceptionHandler;
import com.eventhub.backend.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String CSRF_HEADER = "X-CSRF-Protection";
    private static final Set<String> PUBLIC_AUTH_PATHS = Set.of(
            "/api/auth/register", "/api/auth/login", "/api/auth/refresh", "/api/auth/logout");
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");
    private final AuthService auth;
    private final AuthCookies cookies;
    private final GlobalExceptionHandler errors;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        // Browsers must pass CORS preflight to send this header cross-origin.
        // Enforce it before public auth routes too, so login/logout remain CSRF-protected.
        if (!SAFE_METHODS.contains(request.getMethod()) && !"1".equals(request.getHeader(CSRF_HEADER))) {
            errors.handle(request, response, new AccessDeniedException("Missing CSRF protection header"));
            return;
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if ("POST".equals(request.getMethod()) && PUBLIC_AUTH_PATHS.contains(path)) {
            chain.doFilter(request, response);
            return;
        }
        String token = cookies.read(request, AuthCookies.ACCESS);
        if (token != null) {
            try {
                var principal = auth.authenticate(token);
                var authorities = Set.of(new SimpleGrantedAuthority("ROLE_" + principal.role().name()));
                var authentication = UsernamePasswordAuthenticationToken.authenticated(principal, null, authorities);
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (JwtException | AuthenticationException exception) {
                SecurityContextHolder.clearContext();
                errors.commence(request, response, new BadCredentialsException("Invalid access token"));
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
