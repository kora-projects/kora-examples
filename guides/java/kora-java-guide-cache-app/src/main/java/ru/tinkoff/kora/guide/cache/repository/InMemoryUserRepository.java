package ru.tinkoff.kora.guide.cache.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.cache.dto.UserResponse;

@Component
public final class InMemoryUserRepository implements UserRepository {

    private final Map<String, UserResponse> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final AtomicInteger findByIdCalls = new AtomicInteger();

    @Override
    public List<UserResponse> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<UserResponse> findById(String id) {
        this.findByIdCalls.incrementAndGet();
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public String save(String name, String email) {
        String id = String.valueOf(idGenerator.getAndIncrement());
        users.put(id, new UserResponse(id, name, email, LocalDateTime.now()));
        return id;
    }

    @Override
    public boolean update(String id, String name, String email) {
        return users.computeIfPresent(id,
                (k, v) -> new UserResponse(k, name, email, v.createdAt())) != null;
    }

    @Override
    public boolean deleteById(String id) {
        return users.remove(id) != null;
    }

    public int getFindByIdCalls() {
        return this.findByIdCalls.get();
    }

    public void resetStats() {
        this.findByIdCalls.set(0);
    }
}
