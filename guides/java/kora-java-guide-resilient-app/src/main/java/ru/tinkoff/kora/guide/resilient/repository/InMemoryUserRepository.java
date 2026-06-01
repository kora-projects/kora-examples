package ru.tinkoff.kora.guide.resilient.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.resilient.dto.UserResponse;

@Component
public final class InMemoryUserRepository implements UserRepository {

    private final Map<String, UserResponse> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final Map<String, AtomicInteger> getUserTransientFailures = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> updateInvocations = new ConcurrentHashMap<>();
    private final AtomicInteger findAllInvocations = new AtomicInteger();

    @Override
    public List<UserResponse> findAll() {
        this.findAllInvocations.incrementAndGet();

        if (this.users.values().stream().anyMatch(user -> user.name().contains("slow-list"))) {
            sleep(200);
            throw new IllegalStateException("Synthetic list failure for resilience tests");
        }

        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<UserResponse> findById(String id) {
        UserResponse user = users.get(id);
        if (user != null && user.name().contains("retry-user")) {
            AtomicInteger counter = this.getUserTransientFailures.computeIfAbsent(id, ignored -> new AtomicInteger(2));
            if (counter.getAndUpdate(current -> current > 0 ? current - 1 : 0) > 0) {
                throw new IllegalStateException("Synthetic transient read failure for resilience tests");
            }
        }
        return Optional.ofNullable(user);
    }

    @Override
    public String save(String name, String email) {
        if (name.contains("fallback-create")) {
            throw new IllegalStateException("Synthetic create failure for resilience tests");
        }

        String id = String.valueOf(idGenerator.getAndIncrement());
        users.put(id, new UserResponse(id, name, email, LocalDateTime.now()));
        return id;
    }

    @Override
    public boolean update(String id, String name, String email) {
        this.updateInvocations.computeIfAbsent(id, ignored -> new AtomicInteger()).incrementAndGet();
        if (name.contains("breaker-update")) {
            throw new IllegalStateException("Synthetic update failure for resilience tests");
        }

        return users.computeIfPresent(id,
                (k, v) -> new UserResponse(k, name, email, v.createdAt())) != null;
    }

    @Override
    public boolean deleteById(String id) {
        UserResponse user = users.get(id);
        if (user != null && user.name().contains("slow-delete")) {
            sleep(200);
        }
        return users.remove(id) != null;
    }

    public int updateInvocations(String id) {
        return this.updateInvocations.getOrDefault(id, new AtomicInteger()).get();
    }

    public int findAllInvocations() {
        return this.findAllInvocations.get();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Operation interrupted", e);
        }
    }
}
