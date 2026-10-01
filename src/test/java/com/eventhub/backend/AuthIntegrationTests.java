package com.eventhub.backend;

import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.security.AuthCookies;
import com.eventhub.backend.security.JwtService;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import({PostgresTestConfiguration.class, AuthIntegrationTests.ProtectedTestController.class})
@TestPropertySource(locations = "classpath:auth-test.properties")
class AuthIntegrationTests {
    private static final String PASSWORD = "Valid-password-123";
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UserRepository users;
    @Autowired private PasswordEncoder passwords;
    @Autowired private JwtService jwt;
    private final List<Integer> userIds = new ArrayList<>();

    @AfterEach
    void cleanTestData() {
        for (Integer userId : userIds) {
            jdbc.update("DELETE FROM refresh_tokens WHERE users_id = ?", userId);
            jdbc.update("DELETE FROM users WHERE id = ?", userId);
        }
    }

    @Test
    void registrationReturnsOnlyMessageWithoutCreatingSessionOrCookies() throws Exception {
        String email = uniqueEmail();
        MvcResult result = register("  " + email.toUpperCase() + "  ");
        User user = users.findByNormalizedEmail(email).orElseThrow();
        assertThat(user.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(user.getPassword()).startsWith("$2").isNotEqualTo(PASSWORD);
        assertThat(passwords.matches(PASSWORD, user.getPassword())).isTrue();
        assertThat(json.readTree(result.getResponse().getContentAsString()).size()).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM refresh_tokens WHERE users_id = ?",
                Integer.class, user.getId())).isZero();
        assertThat(result.getRequest().getSession(false)).isNull();
        mvc.perform(get("/api/test/authenticated")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/refresh").with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void loginStoresRefreshHashAndUsesConfiguredLifetimesForTokensAndCookies() throws Exception {
        String email = uniqueEmail();
        register(email);
        Session session = login(email, PASSWORD);
        assertThat(jdbc.queryForObject("SELECT token_hash FROM refresh_tokens WHERE users_id = ?", String.class,
                session.userId())).isEqualTo(jwt.hashRefreshToken(session.refresh().getValue())).hasSize(64);
        LocalDateTime created = jdbc.queryForObject("SELECT created_at FROM refresh_tokens WHERE users_id = ?",
                LocalDateTime.class, session.userId());
        LocalDateTime expires = jdbc.queryForObject("SELECT expires_at FROM refresh_tokens WHERE users_id = ?",
                LocalDateTime.class, session.userId());
        assertThat(Duration.between(created, expires).toSeconds()).isBetween(259198L, 259200L);
        assertThat(jdbc.queryForObject("SELECT revoked FROM refresh_tokens WHERE users_id = ?", Boolean.class,
                session.userId())).isFalse();
        assertCookie(session.access(), "/api", 120);
        assertCookie(session.refresh(), "/api/auth", 259200);
        var claims = com.nimbusds.jwt.SignedJWT.parse(session.access().getValue()).getJWTClaimsSet();
        assertThat(Duration.between(claims.getIssueTime().toInstant(), claims.getExpirationTime().toInstant()))
                .isEqualTo(Duration.ofMinutes(2));
        mvc.perform(get("/api/test/authenticated").cookie(session.access())).andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.roles").doesNotExist());
    }

    @ParameterizedTest
    @ValueSource(strings = {"CUSTOMER", "ORGANIZER"})
    void registersRequestedRoleAndPreservesItThroughLoginAndRefresh(String role) throws Exception {
        String email = uniqueEmail();
        mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", PASSWORD,
                                "fullName", "Test user", "role", role, "roles", List.of("ADMIN", "STAFF")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(jsonPath("$.role").doesNotExist())
                .andExpect(header().doesNotExist("Set-Cookie"));
        User user = users.findByNormalizedEmail(email).orElseThrow();
        userIds.add(user.getId());
        assertThat(user.getRole()).isEqualTo(Role.valueOf(role));

        Session loggedIn = login(email, PASSWORD);
        mvc.perform(get("/api/test/authenticated").cookie(loggedIn.access()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value(role));
        mvc.perform(get("/api/test/admin").cookie(loggedIn.access())).andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/refresh").with(csrf()).cookie(loggedIn.refresh()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value(role))
                .andExpect(jsonPath("$.roles").doesNotExist());
    }

    @Test
    void staffAuthorityComesOnlyFromPersistedRoleAndUpdatesOnNextRequest() throws Exception {
        String email = uniqueEmail();
        Session customer = registerAndLogin(email);
        mvc.perform(get("/api/test/staff").cookie(customer.access())).andExpect(status().isForbidden());
        jdbc.update("UPDATE users SET role = 'STAFF' WHERE id = ?", customer.userId());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM event_staffs WHERE staff_id = ?",
                Integer.class, customer.userId())).isZero();
        mvc.perform(get("/api/test/staff").cookie(customer.access())).andExpect(status().isOk());
        Session staff = login(email, PASSWORD);
        mvc.perform(get("/api/test/authenticated").cookie(staff.access()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("STAFF"))
                .andExpect(jsonPath("$.roles").doesNotExist());
        mvc.perform(get("/api/test/customer").cookie(staff.access())).andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/refresh").with(csrf()).cookie(staff.refresh()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("STAFF"))
                .andExpect(jsonPath("$.roles").doesNotExist());
        jdbc.update("UPDATE users SET role = 'CUSTOMER' WHERE id = ?", customer.userId());
        mvc.perform(get("/api/test/staff").cookie(customer.access())).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "STAFF", "UNKNOWN", "customer", ""})
    void rejectsDisallowedRegistrationRoles(String role) throws Exception {
        String email = uniqueEmail();
        mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", PASSWORD,
                                "fullName", "Test user", "role", role))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.role").exists());
        assertThat(users.findByNormalizedEmail(email)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"missing", "null", "number"})
    void requiresExplicitRoleName(String variant) throws Exception {
        String email = uniqueEmail();
        Map<String, Object> body = new HashMap<>(Map.of("email", email, "password", PASSWORD, "fullName", "Test user"));
        if (!variant.equals("missing")) {
            body.put("role", variant.equals("null") ? null : 1);
        }
        mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body))).andExpect(status().isBadRequest());
        assertThat(users.findByNormalizedEmail(email)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"email", "password", "fullName", "phone", "unicodePassword"})
    void rejectsInvalidRegistration(String field) throws Exception {
        String email = field.equals("email") ? "invalid" : uniqueEmail();
        String password = field.equals("password") ? "short" : field.equals("unicodePassword") ? "ậ".repeat(25) : PASSWORD;
        String name = field.equals("fullName") ? " " : "Test user";
        String phone = field.equals("phone") ? "1".repeat(21) : "0123456789";
        mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", password,
                                "fullName", name, "phone", phone, "role", "CUSTOMER"))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        assertThat(users.findByNormalizedEmail(email)).isEmpty();
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() throws Exception {
        String email = uniqueEmail();
        register(email);
        mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(email.toUpperCase())))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("REQUEST_ERROR"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"register", "login", "refresh", "logout"})
    void requiresCustomCsrfHeaderOnEveryAuthEndpoint(String endpoint) throws Exception {
        mvc.perform(post("/api/auth/" + endpoint).contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
        mvc.perform(post("/api/auth/" + endpoint).header("X-CSRF-Protection", "invalid"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/" + endpoint).with(csrf()).header("Origin", "https://untrusted.example"))
                .andExpect(status().isForbidden());
    }

    @Test
    void removedEndpointsAreNotExposed() throws Exception {
        Session session = registerAndLogin(uniqueEmail());
        for (String path : List.of("/api/auth/me", "/api/auth/csrf", "/api/events/1/staff/me")) {
            mvc.perform(get(path).cookie(session.access())).andExpect(status().isNotFound());
        }
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/events/1/staff/1")
                .with(csrf()).cookie(session.access())).andExpect(status().isNotFound());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/events/1/staff/1")
                .with(csrf()).cookie(session.access())).andExpect(status().isNotFound());
    }

    @Test
    void loginCreatesIndependentSessionsAndRejectsWrongCredentialsOrBlockedUsers() throws Exception {
        String email = uniqueEmail();
        Session first = registerAndLogin(email);
        Session second = login(email, PASSWORD);
        assertThat(second.refresh().getValue()).isNotEqualTo(first.refresh().getValue());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM refresh_tokens WHERE users_id = ?", Integer.class,
                first.userId())).isEqualTo(2);
        for (String candidateEmail : List.of(email, uniqueEmail())) {
            mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson(candidateEmail, "incorrect-password")))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        }
        jdbc.update("UPDATE users SET status = 'BLOCKED' WHERE id = ?", first.userId());
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, PASSWORD))).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/test/authenticated").cookie(first.access())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/refresh").with(csrf()).cookie(first.refresh())).andExpect(status().isUnauthorized());
    }

    @Test
    void refreshRotatesTokensAndRejectsOldAccessAndRefreshTokens() throws Exception {
        Session initial = registerAndLogin(uniqueEmail());
        Session rotated = session(mvc.perform(post("/api/auth/refresh").with(csrf()).cookie(initial.refresh()))
                .andExpect(status().isOk()).andReturn());
        assertThat(rotated.refresh().getValue()).isNotEqualTo(initial.refresh().getValue());
        assertThat(jdbc.queryForObject("SELECT revoked FROM refresh_tokens WHERE token_hash = ?",
                Boolean.class, jwt.hashRefreshToken(initial.refresh().getValue()))).isTrue();
        mvc.perform(get("/api/test/authenticated").cookie(initial.access())).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/test/authenticated").cookie(rotated.access())).andExpect(status().isOk());
        mvc.perform(post("/api/auth/refresh").with(csrf()).cookie(initial.refresh()))
                .andExpect(status().isUnauthorized()).andExpect(cookie().maxAge(AuthCookies.ACCESS, 0))
                .andExpect(cookie().maxAge(AuthCookies.REFRESH, 0));
    }

    @Test
    void concurrentRefreshCanSucceedOnlyOnce() throws Exception {
        Session initial = registerAndLogin(uniqueEmail());
        var start = new CountDownLatch(1);
        Callable<Integer> refresh = () -> {
            assertThat(start.await(10, TimeUnit.SECONDS)).isTrue();
            return mvc.perform(post("/api/auth/refresh").with(csrf()).cookie(initial.refresh()))
                    .andReturn().getResponse().getStatus();
        };
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(refresh);
            var second = executor.submit(refresh);
            start.countDown();
            assertThat(List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(200, 401);
        }
        assertThat(jdbc.queryForObject("SELECT count(*) FROM refresh_tokens WHERE users_id = ? AND revoked = false",
                Integer.class, initial.userId())).isEqualTo(1);
    }

    @Test
    void rejectsExpiredMissingAndMalformedRefreshTokens() throws Exception {
        Session session = registerAndLogin(uniqueEmail());
        jdbc.update("""
                UPDATE refresh_tokens SET created_at = (CURRENT_TIMESTAMP AT TIME ZONE 'UTC') - INTERVAL '8 days',
                    expires_at = (CURRENT_TIMESTAMP AT TIME ZONE 'UTC') - INTERVAL '1 day' WHERE users_id = ?
                """, session.userId());
        mvc.perform(post("/api/auth/refresh").with(csrf()).cookie(session.refresh())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/refresh").with(csrf())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/refresh").with(csrf()).cookie(new Cookie(AuthCookies.REFRESH, "bad-token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevokesOnlyCurrentSessionAndClearsMatchingCookiePaths() throws Exception {
        String email = uniqueEmail();
        Session current = registerAndLogin(email);
        Session other = login(email, PASSWORD);
        MvcResult result = mvc.perform(post("/api/auth/logout").with(csrf()).cookie(current.access(), current.refresh()))
                .andExpect(status().isNoContent()).andReturn();
        assertCookie(result.getResponse().getCookie(AuthCookies.ACCESS), "/api", 0);
        assertCookie(result.getResponse().getCookie(AuthCookies.REFRESH), "/api/auth", 0);
        assertThat(jdbc.queryForObject("SELECT revoked FROM refresh_tokens WHERE token_hash = ?", Boolean.class,
                jwt.hashRefreshToken(current.refresh().getValue()))).isTrue();
        mvc.perform(get("/api/test/authenticated").cookie(current.access())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/refresh").with(csrf()).cookie(current.refresh())).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/test/authenticated").cookie(other.access())).andExpect(status().isOk());
        mvc.perform(post("/api/auth/logout").with(csrf())).andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/logout").with(csrf()).cookie(new Cookie(AuthCookies.ACCESS, "bad"), current.refresh()))
                .andExpect(status().isNoContent());
    }

    @Test
    void logoutWithOnlyAccessCookieStillRevokesSession() throws Exception {
        Session session = registerAndLogin(uniqueEmail());
        mvc.perform(post("/api/auth/logout").with(csrf()).cookie(session.access())).andExpect(status().isNoContent());
        mvc.perform(get("/api/test/authenticated").cookie(session.access())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/refresh").with(csrf()).cookie(session.refresh())).andExpect(status().isUnauthorized());
    }

    @Test
    void invalidAccessCookieDoesNotPreventRefreshOrLogin() throws Exception {
        String email = uniqueEmail();
        Session session = registerAndLogin(email);
        Cookie invalidAccess = new Cookie(AuthCookies.ACCESS, "expired-or-tampered");
        mvc.perform(post("/api/auth/refresh").with(csrf()).cookie(invalidAccess, session.refresh()))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").with(csrf()).cookie(invalidAccess).contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, PASSWORD))).andExpect(status().isOk());
    }

    @Test
    void protectedRoutesRejectAnonymousForgedAndWrongRoleRequests() throws Exception {
        mvc.perform(get("/api/test/authenticated")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/test/authenticated").cookie(new Cookie(AuthCookies.ACCESS, "not-a-jwt")))
                .andExpect(status().isUnauthorized());
        Session customer = registerAndLogin(uniqueEmail());
        mvc.perform(get("/api/test/admin").cookie(customer.access())).andExpect(status().isForbidden());
        jdbc.update("UPDATE users SET role = 'ADMIN' WHERE id = ?", customer.userId());
        mvc.perform(get("/api/test/admin").cookie(customer.access())).andExpect(status().isOk());
    }

    @Test
    void enforcesCorsAllowlistAndAllowsCredentialedPreflight() throws Exception {
        mvc.perform(options("/api/auth/login").header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type,X-CSRF-Protection"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(options("/api/auth/login").header("Origin", "https://untrusted.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    void refusesJwtWhoseSubjectDoesNotOwnSession() throws Exception {
        Session first = registerAndLogin(uniqueEmail());
        Session other = registerAndLogin(uniqueEmail());
        String mismatched = jwt.createAccessToken(other.userId(), jwt.verify(first.access().getValue()).sessionId(),
                Role.ADMIN);
        mvc.perform(get("/api/test/authenticated").cookie(new Cookie(AuthCookies.ACCESS, mismatched)))
                .andExpect(status().isUnauthorized());
    }

    private MvcResult register(String email) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful"))
                .andExpect(header().doesNotExist("Set-Cookie")).andReturn();
        userIds.add(users.findByNormalizedEmail(email.strip().toLowerCase(java.util.Locale.ROOT)).orElseThrow().getId());
        return result;
    }

    private Session registerAndLogin(String email) throws Exception {
        register(email);
        return login(email, PASSWORD);
    }

    private RequestPostProcessor csrf() {
        return request -> {
            request.addHeader("X-CSRF-Protection", "1");
            return request;
        };
    }

    private Session login(String email, String password) throws Exception {
        return session(mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email.toUpperCase(), password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").exists())
                .andExpect(jsonPath("$.roles").doesNotExist())
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist()).andReturn());
    }

    private String registrationJson(String email) {
        return json.writeValueAsString(Map.of("email", email, "password", PASSWORD, "fullName", "Test user", "role", "CUSTOMER"));
    }

    private String loginJson(String email, String password) {
        return json.writeValueAsString(Map.of("email", email, "password", password));
    }

    private Session session(MvcResult result) throws Exception {
        return new Session(json.readTree(result.getResponse().getContentAsString()).get("id").asInt(),
                result.getResponse().getCookie(AuthCookies.ACCESS), result.getResponse().getCookie(AuthCookies.REFRESH));
    }

    private String uniqueEmail() {
        return UUID.randomUUID() + "@example.com";
    }

    private void assertCookie(Cookie cookie, String path, int maxAge) {
        assertThat(cookie).isNotNull();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isTrue();
        assertThat(cookie.getPath()).isEqualTo(path);
        assertThat(cookie.getMaxAge()).isEqualTo(maxAge);
        assertThat(cookie.getAttribute("SameSite")).isEqualTo("Lax");
    }

    // Test-only routes exercise the real JWT filter without adding business endpoints.
    @TestComponent
    @RestController
    static class ProtectedTestController {
        @GetMapping("/api/test/authenticated")
        UserResponse authenticated(@AuthenticationPrincipal UserResponse user) {
            return user;
        }

        @GetMapping("/api/test/staff")
        @PreAuthorize("hasRole('STAFF')")
        String staff() {
            return "ok";
        }

        @GetMapping("/api/test/customer")
        @PreAuthorize("hasRole('CUSTOMER')")
        String customer() {
            return "ok";
        }

        @GetMapping("/api/test/admin")
        @PreAuthorize("hasRole('ADMIN')")
        String admin() {
            return "ok";
        }
    }

    private record Session(Integer userId, Cookie access, Cookie refresh) {
    }
}
