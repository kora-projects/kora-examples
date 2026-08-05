package io.koraframework.guide.json.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import io.koraframework.common.annotation.Component;

import io.koraframework.guide.json.dto.UserRequest;
import io.koraframework.guide.json.dto.UserResponse;
import io.koraframework.guide.json.dto.UserResult;


@Component
public final class UserService {

    private final Map<String, UserResponse> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public UserResponse createUser(UserRequest request) {
        String id = String.valueOf(idGenerator.getAndIncrement());
        UserResponse user = new UserResponse(id, request.name(), request.email(), LocalDateTime.now());
        users.put(id, user);
        return user;
    }

    public List<UserResponse> getAllUsers() {
        return users.values().stream().toList();
    }

    public UserResult getUser(String id) {
        UserResponse user = users.get(id);
        if (user != null) {
            return new UserResult.UserSuccess(UserResult.Status.OK, user);
        }
        return new UserResult.UserError(UserResult.Status.ERROR, "User not found with id: " + id);
    }
}


