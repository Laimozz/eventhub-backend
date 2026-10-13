package com.eventhub.backend.service;

import com.eventhub.backend.enums.Role;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@Slf4j
public class NotificationStreamService {
    private final AuthService auth;
    private final TaskExecutor executor;
    private final ConcurrentHashMap<Integer, Set<Subscription>> subscriptions = new ConcurrentHashMap<>();

    public NotificationStreamService(AuthService auth, @Qualifier("notificationExecutor") TaskExecutor executor) {
        this.auth = auth;
        this.executor = executor;
    }

    public SseEmitter subscribe(Integer userId, String accessToken) {
        var emitter = new SseEmitter(120_000L);
        var subscription = new Subscription(userId, accessToken, emitter);
        subscriptions.compute(userId, (id, existing) -> {
            var clients = existing == null ? ConcurrentHashMap.<Subscription>newKeySet() : existing;
            clients.add(subscription);
            return clients;
        });
        emitter.onCompletion(() -> remove(subscription));
        emitter.onTimeout(() -> { remove(subscription); emitter.complete(); });
        emitter.onError(error -> remove(subscription));
        send(subscription, SseEmitter.event().name("connected").data("ready"));
        return emitter;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void changed(NotificationService.Changed event) {
        try {
            executor.execute(() -> {
                var clients = subscriptions.get(event.userId());
                if (clients != null) clients.forEach(client ->
                        send(client, SseEmitter.event().name("notifications-changed").data("refresh")));
            });
        } catch (TaskRejectedException exception) {
            // The persisted list remains authoritative; the next connection reloads it.
            log.warn("Notification executor is full; user {} will resync on reconnect", event.userId());
        }
    }

    @Scheduled(fixedDelay = 25_000)
    public void heartbeat() {
        subscriptions.values().forEach(clients -> clients.forEach(client ->
                send(client, SseEmitter.event().comment("heartbeat"))));
    }

    private void send(Subscription client, SseEmitter.SseEventBuilder event) {
        try {
            // A long-lived connection must stop when its session expires, is revoked or is locked.
            var user = auth.authenticate(client.accessToken);
            if (!user.id().equals(client.userId) || user.role() == Role.STAFF) {
                remove(client);
                client.emitter.complete();
                return;
            }
            client.emitter.send(event);
        } catch (AuthenticationException | JwtException exception) {
            remove(client);
            client.emitter.complete();
        } catch (IOException | IllegalStateException exception) {
            remove(client);
            // Spring completes an SSE response after an IO failure.
        } catch (RuntimeException exception) {
            remove(client);
            client.emitter.complete();
            log.warn("Could not deliver notification to user {} ({})", client.userId, exception.getClass().getSimpleName());
        }
    }

    private void remove(Subscription client) {
        subscriptions.computeIfPresent(client.userId, (id, clients) -> {
            clients.remove(client);
            return clients.isEmpty() ? null : clients;
        });
    }

    @PreDestroy
    public void close() {
        subscriptions.values().forEach(clients -> clients.forEach(client -> client.emitter.complete()));
        subscriptions.clear();
    }

    private static final class Subscription {
        final Integer userId;
        final String accessToken;
        final SseEmitter emitter;

        Subscription(Integer userId, String accessToken, SseEmitter emitter) {
            this.userId = userId;
            this.accessToken = accessToken;
            this.emitter = emitter;
        }
    }
}
