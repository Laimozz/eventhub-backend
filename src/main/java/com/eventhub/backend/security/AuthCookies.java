package com.eventhub.backend.security;

import com.eventhub.backend.config.ApplicationProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthCookies {
    public static final String ACCESS = "access_token";
    public static final String REFRESH = "refresh_token";
    private final ApplicationProperties properties;

    public String read(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies).filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue).findFirst().orElse(null);
    }

    public void write(HttpServletResponse response, String accessToken, String refreshToken) {
        add(response, ACCESS, accessToken, "/api", properties.getAuth().getAccessTokenTtl());
        add(response, REFRESH, refreshToken, "/api/auth", properties.getAuth().getRefreshTokenTtl());
    }

    public void clear(HttpServletResponse response) {
        add(response, ACCESS, "", "/api", Duration.ZERO);
        add(response, REFRESH, "", "/api/auth", Duration.ZERO);
    }

    private void add(HttpServletResponse response, String name, String value, String path, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value).httpOnly(true)
                .secure(properties.getAuth().isCookieSecure()).sameSite(properties.getAuth().getCookieSameSite())
                .path(path).maxAge(maxAge).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
