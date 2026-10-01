package com.eventhub.backend.service;

import com.eventhub.backend.config.ApplicationProperties;
import com.eventhub.backend.dto.request.LoginRequest;
import com.eventhub.backend.dto.request.RegisterRequest;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.entity.RefreshToken;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.repository.RefreshTokenRepository;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AuthService {
    private final UserRepository users;
    private final RefreshTokenRepository tokens;
    private final PasswordEncoder passwords;
    private final JwtService jwt;
    private final ApplicationProperties properties;
    private final Clock clock;
    private final String dummyPasswordHash;

    public AuthService(UserRepository users, RefreshTokenRepository tokens, PasswordEncoder passwords,
            JwtService jwt, ApplicationProperties properties, Clock clock) {
        this.users = users;
        this.tokens = tokens;
        this.passwords = passwords;
        this.jwt = jwt;
        this.properties = properties;
        this.clock = clock;
        this.dummyPasswordHash = passwords.encode(UUID.randomUUID().toString());
    }

    public void register(RegisterRequest request) {
        if (users.findByNormalizedEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwords.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setRole(Role.valueOf(request.role()));
        user.setStatus("ACTIVE");
        users.saveAndFlush(user);
    }

    public AuthSession login(LoginRequest request) {
        User user = users.findByNormalizedEmail(request.email()).orElse(null);
        // Equal-cost password checks for unknown email addresses reduce account enumeration by timing.
        boolean passwordMatches = request.password().getBytes(StandardCharsets.UTF_8).length <= 72
                && passwords.matches(request.password(), user == null ? dummyPasswordHash : user.getPassword());
        if (user == null || !passwordMatches || !user.isActive()) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return issueSession(user);
    }

    public AuthSession refresh(String refreshToken) {
        RefreshToken existing = tokens.findByHashForUpdate(jwt.hashRefreshToken(refreshToken))
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        LocalDateTime now = LocalDateTime.now(clock);
        if (!existing.isActive(now) || !existing.getUser().isActive()) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        // The row lock ensures a refresh token can be used only once, including concurrent requests.
        existing.setRevoked(true);
        return issueSession(existing.getUser());
    }

    public void logout(String refreshToken, String accessToken) {
        if (refreshToken != null) {
            try {
                tokens.findByHashForUpdate(jwt.hashRefreshToken(refreshToken))
                        .ifPresent(token -> token.setRevoked(true));
            } catch (BadCredentialsException ignored) {
                // Logout is idempotent even if the browser has a malformed refresh cookie.
            }
        }
        if (accessToken != null) {
            try {
                tokens.revokeById(jwt.verify(accessToken).sessionId());
            } catch (JwtException ignored) {
                // An expired/invalid access cookie must not prevent refresh revocation or cookie cleanup.
            }
        }
    }

    @Transactional(readOnly = true)
    public UserResponse authenticate(String accessToken) {
        var identity = jwt.verify(accessToken);
        RefreshToken session = tokens.findSessionById(identity.sessionId())
                .orElseThrow(() -> new BadCredentialsException("Invalid session"));
        if (!session.isActive(LocalDateTime.now(clock)) || !session.getUser().isActive()
                || !session.getUser().getId().equals(identity.userId())) {
            throw new BadCredentialsException("Invalid session");
        }
        return UserResponse.from(session.getUser());
    }

    private AuthSession issueSession(User user) {
        String rawRefreshToken = jwt.createRefreshToken();
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(jwt.hashRefreshToken(rawRefreshToken));
        token.setExpiresAt(LocalDateTime.now(clock).plus(properties.getAuth().getRefreshTokenTtl()));
        tokens.saveAndFlush(token);
        return new AuthSession(UserResponse.from(user),
                jwt.createAccessToken(user.getId(), token.getId(), user.getRole()), rawRefreshToken);
    }

    // Internal result: tokens are written to cookies, never returned in JSON.
    public record AuthSession(UserResponse user, String accessToken, String refreshToken) {
        @Override
        public String toString() {
            return "AuthSession[userId=" + user.id() + "]";
        }
    }
}
