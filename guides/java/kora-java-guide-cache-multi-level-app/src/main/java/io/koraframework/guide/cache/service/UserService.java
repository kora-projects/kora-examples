package io.koraframework.guide.cache.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import io.koraframework.cache.annotation.CacheInvalidate;
import io.koraframework.cache.annotation.CachePut;
import io.koraframework.cache.annotation.Cacheable;
import io.koraframework.common.annotation.Component;
import io.koraframework.guide.cache.dto.UserRequest;
import io.koraframework.guide.cache.dto.UserResponse;
import io.koraframework.guide.cache.repository.UserRepository;
import io.koraframework.http.server.common.response.HttpServerResponseException;
import io.koraframework.json.common.annotation.Json;

@Component
public class UserService {

    private final UserRepository userRepository;
    private final UserCaffeineCache userCaffeineCache;
    private final UserRedisCache userRedisCache;

    public UserService(UserRepository userRepository, UserCaffeineCache userCaffeineCache, UserRedisCache userRedisCache) {
        this.userRepository = userRepository;
        this.userCaffeineCache = userCaffeineCache;
        this.userRedisCache = userRedisCache;
    }

    public UserResponse createUser(UserRequest request) {
        var generatedId = userRepository.save(request.name(), request.email());
        var createdUser = new UserResponse(generatedId, request.name(), request.email(), LocalDateTime.now());
        this.userCaffeineCache.put(createdUser.id(), createdUser);
        this.userRedisCache.put(createdUser.id(), createdUser);
        return createdUser;
    }

    @Cacheable(UserCaffeineCache.class)
    @Cacheable(UserRedisCache.class)
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

    @CachePut(value = UserCaffeineCache.class, args = { "id" })
    @CachePut(value = UserRedisCache.class, args = { "id" })
    public UserResponse updateUser(String id, UserRequest request) {
        boolean updated = userRepository.update(id, request.name(), request.email());
        if (!updated) {
            throw HttpServerResponseException.of(404, "User not found");
        }
        return new UserResponse(id, request.name(), request.email(), LocalDateTime.now());
    }

    @CacheInvalidate(UserCaffeineCache.class)
    @CacheInvalidate(UserRedisCache.class)
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
