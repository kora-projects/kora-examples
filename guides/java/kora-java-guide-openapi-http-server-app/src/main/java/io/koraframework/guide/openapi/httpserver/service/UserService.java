package io.koraframework.guide.openapi.httpserver.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import io.koraframework.common.annotation.Component;
import io.koraframework.guide.openapi.httpserver.dto.UserRequest;
import io.koraframework.guide.openapi.httpserver.dto.UserResponse;
import io.koraframework.guide.openapi.httpserver.repository.UserRepository;
import io.koraframework.http.server.common.response.HttpServerResponseException;

@Component
public final class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(UserRequest request) {
        var generatedId = this.userRepository.save(request.name(), request.email());
        return new UserResponse(generatedId, request.name(), request.email(), LocalDateTime.now());
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
        return new UserResponse(id, request.name(), request.email(), LocalDateTime.now());
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

