package ru.tinkoff.kora.guide.cache.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import ru.tinkoff.kora.cache.annotation.CacheInvalidate;
import ru.tinkoff.kora.cache.annotation.CachePut;
import ru.tinkoff.kora.cache.annotation.Cacheable;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.cache.dto.UserRequest;
import ru.tinkoff.kora.guide.cache.dto.UserResponse;
import ru.tinkoff.kora.guide.cache.repository.UserRepository;
import ru.tinkoff.kora.http.server.common.HttpServerResponseException;

@Component
public class UserService {

    private final UserRepository userRepository;
    private final UserCaffeineCache userCache;

    public UserService(UserRepository userRepository, UserCaffeineCache userCache) {
        this.userRepository = userRepository;
        this.userCache = userCache;
    }

    public UserResponse createUser(UserRequest request) {
        var generatedId = userRepository.save(request.name(), request.email());
        var createdUser = new UserResponse(generatedId, request.name(), request.email(), LocalDateTime.now());
        this.userCache.put(createdUser.id(), createdUser);
        return createdUser;
    }

    @Cacheable(UserCaffeineCache.class)
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

    @CachePut(value = UserCaffeineCache.class, parameters = { "id" })
    public UserResponse updateUser(String id, UserRequest request) {
        boolean updated = userRepository.update(id, request.name(), request.email());
        if (!updated) {
            throw HttpServerResponseException.of(404, "User not found");
        }
        return new UserResponse(id, request.name(), request.email(), LocalDateTime.now());
    }

    @CacheInvalidate(UserCaffeineCache.class)
    public void deleteUser(String id) {
        boolean deleted = userRepository.deleteById(id);
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

