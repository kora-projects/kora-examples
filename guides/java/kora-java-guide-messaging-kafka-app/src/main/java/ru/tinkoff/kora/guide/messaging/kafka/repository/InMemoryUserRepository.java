package ru.tinkoff.kora.guide.messaging.kafka.repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.messaging.kafka.dto.UserResponse;

@Component
public final class InMemoryUserRepository implements UserRepository {

    private final Map<String, UserResponse> users = new ConcurrentHashMap<>();

    @Override
    public void save(UserResponse user) {
        this.users.put(user.id(), user);
    }

    @Override
    public List<UserResponse> findAll() {
        return new ArrayList<>(this.users.values());
    }

    @Override
    public Optional<UserResponse> findById(String id) {
        return Optional.ofNullable(this.users.get(id));
    }

    @Override
    public boolean update(String id, String name, String email) {
        return this.users.computeIfPresent(id,
                (key, current) -> new UserResponse(key, name, email, current.createdAt())) != null;
    }

    @Override
    public boolean deleteById(String id) {
        return this.users.remove(id) != null;
    }
}
