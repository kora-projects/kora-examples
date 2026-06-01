package ru.tinkoff.kora.guide.databasecassandra.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.databasecassandra.dto.UserRequest;
import ru.tinkoff.kora.guide.databasecassandra.dto.UserResponse;
import ru.tinkoff.kora.guide.databasecassandra.repository.UserDAO;
import ru.tinkoff.kora.guide.databasecassandra.repository.UserRepository;
import ru.tinkoff.kora.http.server.common.HttpServerResponseException;

@Component
public final class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(UserRequest request) {
        var user = new UserDAO(UUID.randomUUID().toString(), request.name(), request.email(), Instant.now());
        userRepository.save(user);
        return toResponse(user);
    }

    public Optional<UserResponse> getUser(String id) {
        return Optional.ofNullable(userRepository.findById(id)).map(this::toResponse);
    }

    public List<UserResponse> getUsers(int page, int size, String sort) {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .sorted(getComparator(sort))
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    public UserResponse updateUser(String id, UserRequest request) {
        var existing = userRepository.findById(id);
        if (existing == null) {
            throw HttpServerResponseException.of(404, "User not found");
        }
        var updated = new UserDAO(id, request.name(), request.email(), existing.createdAt());
        userRepository.update(updated);
        return toResponse(updated);
    }

    public void deleteUser(String id) {
        if (userRepository.findById(id) == null) {
            throw HttpServerResponseException.of(404, "User not found");
        }
        userRepository.deleteById(id);
    }

    private UserResponse toResponse(UserDAO user) {
        return new UserResponse(user.id(), user.name(), user.email(), user.createdAt());
    }

    private Comparator<UserResponse> getComparator(String sort) {
        return switch (sort.toLowerCase()) {
            case "name" -> Comparator.comparing(UserResponse::name);
            case "email" -> Comparator.comparing(UserResponse::email);
            case "createdat" -> Comparator.comparing(UserResponse::createdAt);
            default -> Comparator.comparing(UserResponse::name);
        };
    }
}
