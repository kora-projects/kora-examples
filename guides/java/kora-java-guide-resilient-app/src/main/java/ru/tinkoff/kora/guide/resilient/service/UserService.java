package ru.tinkoff.kora.guide.resilient.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.resilient.dto.UserRequest;
import ru.tinkoff.kora.guide.resilient.dto.UserResponse;
import ru.tinkoff.kora.guide.resilient.repository.UserRepository;
import ru.tinkoff.kora.http.server.common.HttpServerResponseException;
import ru.tinkoff.kora.resilient.circuitbreaker.annotation.CircuitBreaker;
import ru.tinkoff.kora.resilient.fallback.annotation.Fallback;
import ru.tinkoff.kora.resilient.retry.annotation.Retry;
import ru.tinkoff.kora.resilient.timeout.annotation.Timeout;

@Component
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Fallback(value = "default", method = "createUserFallback(request)")
    public UserResponse createUser(UserRequest request) {
        var generatedId = userRepository.save(request.name(), request.email());
        return new UserResponse(generatedId, request.name(), request.email(), LocalDateTime.now());
    }

    @Retry("default")
    public Optional<UserResponse> getUser(String id) {
        return userRepository.findById(id);
    }

    @CircuitBreaker("default")
    @Retry("default")
    @Timeout("default")
    public List<UserResponse> getUsers(int page, int size, String sort) {
        return userRepository.findAll().stream()
                .sorted(getComparator(sort))
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    @CircuitBreaker("default")
    public UserResponse updateUser(String id, UserRequest request) {
        boolean updated = userRepository.update(id, request.name(), request.email());
        if (!updated) {
            throw HttpServerResponseException.of(404, "User not found");
        }
        return new UserResponse(id, request.name(), request.email(), LocalDateTime.now());
    }

    @Timeout("default")
    public void deleteUser(String id) {
        boolean deleted = userRepository.deleteById(id);
        if (!deleted) {
            throw HttpServerResponseException.of(404, "User not found");
        }
    }

    protected UserResponse createUserFallback(UserRequest request) {
        // Never do this in real systems: imagine we wrote the request to a file
        // and planned to recreate the user during application startup.
        return new UserResponse("pending-file-write", request.name(), request.email(), LocalDateTime.now());
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
