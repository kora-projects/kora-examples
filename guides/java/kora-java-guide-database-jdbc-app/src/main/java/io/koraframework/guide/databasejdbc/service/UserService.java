package io.koraframework.guide.databasejdbc.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import io.koraframework.common.annotation.Component;
import io.koraframework.guide.databasejdbc.dto.UserRequest;
import io.koraframework.guide.databasejdbc.dto.UserResponse;
import io.koraframework.guide.databasejdbc.repository.UserDAO;
import io.koraframework.guide.databasejdbc.repository.UserRepository;
import io.koraframework.http.server.common.response.HttpServerResponseException;

@Component
public final class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(UserRequest request) {
        var generatedId = userRepository.save(request.name(), request.email());
        return new UserResponse(String.valueOf(generatedId), request.name(), request.email(), LocalDateTime.now());
    }

    public Optional<UserResponse> getUser(String id) {
        return parseId(id).flatMap(userRepository::findById).map(this::toResponse);
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
        var parsedId = parseIdOrThrow(id);
        var updated = userRepository.update(parsedId, request.name(), request.email());
        if (updated.value() < 1) {
            throw HttpServerResponseException.of(404, "User not found");
        }
        return new UserResponse(String.valueOf(parsedId), request.name(), request.email(), LocalDateTime.now());
    }

    public void deleteUser(String id) {
        var parsedId = parseIdOrThrow(id);
        var deleted = userRepository.deleteById(parsedId);
        if (deleted.value() < 1) {
            throw HttpServerResponseException.of(404, "User not found");
        }
    }

    private long parseIdOrThrow(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException ignored) {
            throw HttpServerResponseException.of(400, "Invalid user id: " + id);
        }
    }

    private Optional<Long> parseId(String id) {
        try {
            return Optional.of(Long.parseLong(id));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    private UserResponse toResponse(UserDAO user) {
        return new UserResponse(String.valueOf(user.id()), user.name(), user.email(), user.createdAt());
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
