package com.eventhub.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventhub.backend.dto.request.LoginRequest;
import com.eventhub.backend.dto.request.RegisterRequest;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.security.AuthCookies;
import com.eventhub.backend.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;
    private final AuthCookies cookies;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest body) {
        auth.register(body);
        return ResponseEntity.status(HttpStatus.CREATED).body("Registration successful");
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest body,
            HttpServletResponse response) {
        var session = auth.login(body);
        cookies.write(response, session.accessToken(), session.refreshToken());
        return ResponseEntity.ok(session.user());
    }

    @PostMapping("/refresh")
    public ResponseEntity<UserResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        try {
            var session = auth.refresh(cookies.read(request, AuthCookies.REFRESH));
            cookies.write(response, session.accessToken(), session.refreshToken());
            return ResponseEntity.ok(session.user());
        } catch (AuthenticationException exception) {
            cookies.clear(response);
            throw exception;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        auth.logout(cookies.read(request, AuthCookies.REFRESH), cookies.read(request, AuthCookies.ACCESS));
        cookies.clear(response);
        return ResponseEntity.noContent().build();
    }
}
