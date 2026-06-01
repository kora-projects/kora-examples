package ru.tinkoff.kora.guide.messaging.kafka.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.messaging.kafka.dto.UserRequest;
import ru.tinkoff.kora.guide.messaging.kafka.dto.UserResponse;
import ru.tinkoff.kora.guide.messaging.kafka.kafka.UserCreatedEvent;
import ru.tinkoff.kora.guide.messaging.kafka.repository.UserRepository;
import ru.tinkoff.kora.http.server.common.HttpServerResponseException;

@Component
public final class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void createUser(UserCreatedEvent event) {
        this.userRepository.save(new UserResponse(event.id(), event.name(), event.email(), event.createdAt()));
    }

    public Optional<UserResponse> getUser(String id) {
        return this.userRepository.findById(id);
    }

    public List<UserResponse> getUsers(int page, int size, String sort) {
        return this.userRepository.findAll().stream()
                .sorted(this.getComparator(sort))
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    public UserResponse updateUser(String id, UserRequest request) {
        boolean updated = this.userRepository.update(id, request.name(), request.email());
        if (!updated) {
            throw HttpServerResponseException.of(404, "User not found");
        }
        return this.userRepository.findById(id).orElseThrow();
    }

    public void deleteUser(String id) {
        boolean deleted = this.userRepository.deleteById(id);
        if (!deleted) {
            throw HttpServerResponseException.of(404, "User not found");
        }
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
