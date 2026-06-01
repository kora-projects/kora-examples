package ru.tinkoff.kora.guide.grpcserver.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.grpcserver.dto.UserRequest;
import ru.tinkoff.kora.guide.grpcserver.dto.UserResponse;
import ru.tinkoff.kora.guide.grpcserver.repository.UserRepository;

@Component
public final class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(UserRequest request) {
        var generatedId = userRepository.save(request.name(), request.email());
        return new UserResponse(generatedId, request.name(), request.email(), LocalDateTime.now());
    }

    public Optional<UserResponse> getUser(String id) {
        return userRepository.findById(id);
    }

    public List<UserResponse> getUsers(int page, int size, String sort) {
        return userRepository.findAll().stream()
                .sorted(getComparator(sort))
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    public UserResponse updateUser(String id, UserRequest request) {
        boolean updated = userRepository.update(id, request.name(), request.email());
        if (!updated) {
            throw new UserNotFoundException(id);
        }
        return new UserResponse(id, request.name(), request.email(), LocalDateTime.now());
    }

    public void deleteUser(String id) {
        boolean deleted = userRepository.deleteById(id);
        if (!deleted) {
            throw new UserNotFoundException(id);
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
