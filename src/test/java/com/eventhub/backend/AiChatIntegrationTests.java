package com.eventhub.backend;

import com.eventhub.backend.dto.response.UserResponse;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.enums.Role;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.security.AuthCookies;
import com.eventhub.backend.service.AiContextService;
import com.eventhub.backend.service.AiConversationService;
import com.eventhub.backend.service.OpenRouterClient;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
@TestPropertySource(locations = "classpath:auth-test.properties")
@Transactional
class AiChatIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired AiContextService context;
    @Autowired AiConversationService conversations;
    @MockitoBean OpenRouterClient provider;

    private static final String ANSWER = "**EventHub** hỗ trợ bạn tra cứu thông tin. ".repeat(20);

    @BeforeEach
    void setup() {
        when(provider.available()).thenReturn(true);
        when(provider.complete(anyList())).thenReturn(ANSWER);
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    @SuppressWarnings({"unchecked", "rawtypes"})
    void allFourRolesCanChatAndPersistLongAnswers(Role role) throws Exception {
        var user = user(role);
        var cookie = login(user);
        var response = send(cookie, Map.of("message", "Cho tôi biết thông tin cần thiết", "role", "ADMIN"));
        int id = response.path("conversation").path("id").asInt();
        mvc.perform(get("/api/ai/conversations/" + id).cookie(cookie)).andExpect(status().isOk())
                .andExpect(jsonPath("$.messages.length()").value(2))
                .andExpect(jsonPath("$.messages[1].content").value(ANSWER));
        mvc.perform(get("/api/ai/conversations").cookie(cookie)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
        ArgumentCaptor<List<OpenRouterClient.ChatMessage>> captor = ArgumentCaptor.forClass((Class) List.class);
        verify(provider).complete(captor.capture());
        var prompt = captor.getValue();
        assertThat(prompt.get(1).content()).contains("\"role\":\"" + role.name() + "\"");
        assertThat(prompt.getLast().role()).isEqualTo("user");
        send(cookie, Map.of("conversationId", id, "message", "Giải thích thêm"));
        mvc.perform(get("/api/ai/conversations/" + id).cookie(cookie)).andExpect(jsonPath("$.messages.length()").value(4));
        mvc.perform(delete("/api/ai/conversations/" + id).cookie(cookie).header("X-CSRF-Protection", "1"))
                .andExpect(status().isNoContent());
        assertThat(jdbc.queryForObject("select count(*) from ai_messages where ai_conversations_id=?", Long.class, id)).isZero();
    }

    @Test
    void conversationsAreIsolatedByOwnerAndRole() throws Exception {
        var owner = user(Role.CUSTOMER);
        var other = user(Role.CUSTOMER);
        int id = send(login(owner), Map.of("message", "Thông tin đặt vé"))
                .path("conversation").path("id").asInt();
        var otherCookie = login(other);
        clearInvocations(provider);
        mvc.perform(get("/api/ai/conversations/" + id).cookie(otherCookie)).andExpect(status().isNotFound());
        mvc.perform(post("/api/ai/chat").cookie(otherCookie).header("X-CSRF-Protection", "1")
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("conversationId", id, "message", "Đọc hội thoại"))))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/ai/conversations/" + id).cookie(otherCookie).header("X-CSRF-Protection", "1"))
                .andExpect(status().isNotFound());
        verify(provider, never()).complete(anyList());
        jdbc.update("update users set role='STAFF' where id=?", owner.getId());
        users.flush();
        var staffView = new UserResponse(owner.getId(), owner.getEmail(), null, null, Role.STAFF);
        assertThat(conversations.list(staffView)).isEmpty();
        assertThatThrownBy(() -> conversations.detail(staffView, id)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void contextOnlyContainsAuthorizedRowsAndNeverCredentialsOrQrTokens() {
        var organizer = user(Role.ORGANIZER);
        var anotherOrganizer = user(Role.ORGANIZER);
        int mine = event(organizer.getId(), "My private draft", "PENDING_APPROVAL");
        int foreign = event(anotherOrganizer.getId(), "Other private draft", "PENDING_APPROVAL");
        int publicEvent = event(anotherOrganizer.getId(), "Public concert", "APPROVED");
        var staff = user(Role.STAFF);
        jdbc.update("insert into event_staffs(events_id,staff_id,assigned_by,status) values (?,?,?,'ACTIVE')", mine, staff.getId(), organizer.getId());
        jdbc.update("insert into event_staffs(events_id,staff_id,assigned_by,status) values (?,?,?,'INACTIVE')", foreign, staff.getId(), anotherOrganizer.getId());
        String organizerContext = context.load(UserResponse.from(organizer), "sự kiện").data();
        assertThat(organizerContext).contains("My private draft").doesNotContain("Other private draft", "Public concert");
        String staffContext = context.load(UserResponse.from(staff), "sự kiện").data();
        assertThat(staffContext).contains("My private draft").doesNotContain("Other private draft", "Public concert");
        var customer = user(Role.CUSTOMER);
        var otherCustomer = user(Role.CUSTOMER);
        jdbc.update("insert into bookings(users_id,events_id,total_amount,status,expired_at) values (?, ?, 123456, 'PAID', current_timestamp + interval '1 hour')", customer.getId(), publicEvent);
        jdbc.update("insert into bookings(users_id,events_id,total_amount,status,expired_at) values (?, ?, 987654, 'PAID', current_timestamp + interval '1 hour')", otherCustomer.getId(), publicEvent);
        String customerContext = context.load(UserResponse.from(customer), "sự kiện").data();
        assertThat(customerContext).contains("Public concert", "123456").doesNotContain("private draft", "987654");
        for (String snapshot : List.of(organizerContext, staffContext, customerContext)) {
            assertThat(snapshot).doesNotContain("qr_token", "password", "account_number", "email", "phone");
        }
        var adminContext = context.load(UserResponse.from(user(Role.ADMIN)), "tổng quan").data();
        assertThat(adminContext).contains("overview", "pendingEvents", "My private draft", "Other private draft");
    }

    @Test
    void providerFailureDoesNotSavePartialConversation() throws Exception {
        var owner = user(Role.CUSTOMER);
        when(provider.complete(anyList())).thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI đang bận"));
        mvc.perform(post("/api/ai/chat").cookie(login(owner)).header("X-CSRF-Protection", "1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"Xin chào\"}"))
                .andExpect(status().isServiceUnavailable());
        assertThat(conversations.list(UserResponse.from(owner))).isEmpty();
    }

    @Test
    void requiresAuthenticationCsrfAndValidQuestion() throws Exception {
        mvc.perform(get("/api/ai/status")).andExpect(status().isUnauthorized());
        var cookie = login(user(Role.CUSTOMER));
        mvc.perform(post("/api/ai/chat").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"hello\"}"))
                .andExpect(status().isForbidden());
        for (String message : List.of("   ", "x".repeat(2001))) {
            mvc.perform(post("/api/ai/chat").cookie(cookie).header("X-CSRF-Protection", "1")
                    .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("message", message))))
                    .andExpect(status().isBadRequest());
        }
        verify(provider, never()).complete(anyList());
    }

    private User user(Role role) {
        var user = new User();
        user.setEmail(UUID.randomUUID() + "@chat.test");
        user.setPassword(encoder.encode("Test-password-123"));
        user.setFullName("Test User");
        user.setRole(role);
        user.setStatus("ACTIVE");
        return users.saveAndFlush(user);
    }

    private Cookie login(User user) throws Exception {
        return mvc.perform(post("/api/auth/login").header("X-CSRF-Protection", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", user.getEmail(), "password", "Test-password-123"))))
                .andExpect(status().isOk()).andReturn().getResponse().getCookie(AuthCookies.ACCESS);
    }

    private JsonNode send(Cookie cookie, Map<String, Object> request) throws Exception {
        return json.readTree(mvc.perform(post("/api/ai/chat").cookie(cookie).header("X-CSRF-Protection", "1")
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(request)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    private int event(int organizer, String name, String status) {
        int venue = jdbc.queryForObject("insert into venues(city,address,capacity) values ('Hồ Chí Minh','Test venue',100) returning id", Integer.class);
        int category = jdbc.queryForObject("insert into categories(name) values (?) returning id", Integer.class, UUID.randomUUID().toString());
        int event = jdbc.queryForObject("""
                insert into events(organizer_id,venues_id,categories_id,name,thumbnail_image_url,banner_image_url,start_time,end_time,status)
                values (?,?,?,?,'https://example.test/a.png','https://example.test/b.png',current_timestamp+interval '1 day',current_timestamp+interval '2 days',?) returning id
                """, Integer.class, organizer, venue, category, name, status);
        jdbc.update("""
                insert into ticket_types(events_id,name,image_url,price,quantity,remaining_quantity,sale_start_time,sale_end_time,status)
                values (?,'Standard','https://example.test/t.png',100000,100,100,current_timestamp-interval '1 day',current_timestamp+interval '1 day','ACTIVE')
                """, event);
        return event;
    }
}
