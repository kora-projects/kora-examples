package ru.tinkoff.kora.guide.observability.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.observability.dto.UserRequest;
import ru.tinkoff.kora.guide.observability.dto.UserResponse;
import ru.tinkoff.kora.guide.observability.repository.UserRepository;
import ru.tinkoff.kora.http.server.common.HttpServerResponseException;

@Component
public final class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final MetricsService metricsService;

    public UserService(UserRepository userRepository, MetricsService metricsService) {
        this.userRepository = userRepository;
        this.metricsService = metricsService;
    }

    public UserResponse createUser(UserRequest request) {
        logger.info("Creating user with name={} and email={}", request.name(), request.email());
        return metricsService.recordUserCreation(() -> {
            var generatedId = userRepository.save(request.name(), request.email());
            var user = new UserResponse(generatedId, request.name(), request.email(), LocalDateTime.now());
            logger.info("Created user with id={}", generatedId);
            return user;
        });
    }

    public Optional<UserResponse> getUser(String id) {
        logger.debug("Retrieving user with id={}", id);
        return userRepository.findById(id);
    }

    public List<UserResponse> getUsers(int page, int size, String sort) {
        logger.debug("Listing users page={}, size={}, sort={}", page, size, sort);
        return userRepository.findAll().stream()
                .sorted(getComparator(sort))
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    public UserResponse updateUser(String id, UserRequest request) {
        boolean updated = userRepository.update(id, request.name(), request.email());
        if (!updated) {
            logger.warn("User with id={} was not found for update", id);
            throw HttpServerResponseException.of(404, "User not found");
        }
        logger.info("Updated user with id={}", id);
        return new UserResponse(id, request.name(), request.email(), LocalDateTime.now());
    }

    public void deleteUser(String id) {
        boolean deleted = userRepository.deleteById(id);
        if (!deleted) {
            logger.warn("User with id={} was not found for deletion", id);
            throw HttpServerResponseException.of(404, "User not found");
        }
        logger.info("Deleted user with id={}", id);
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
