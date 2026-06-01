package ru.tinkoff.kora.guide.messaging.kafka.repository;

import java.util.List;
import java.util.Optional;
import ru.tinkoff.kora.guide.messaging.kafka.dto.UserResponse;

public interface UserRepository {

    void save(UserResponse user);

    List<UserResponse> findAll();

    Optional<UserResponse> findById(String id);

    boolean update(String id, String name, String email);

    boolean deleteById(String id);
}
