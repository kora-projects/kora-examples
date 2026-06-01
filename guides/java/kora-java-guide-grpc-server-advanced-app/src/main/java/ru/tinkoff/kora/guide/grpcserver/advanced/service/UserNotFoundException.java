package ru.tinkoff.kora.guide.grpcserver.advanced.service;

public final class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String userId) {
        super("User not found: " + userId);
    }
}
