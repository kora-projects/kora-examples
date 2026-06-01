package ru.tinkoff.kora.guide.grpcserver.service

class UserNotFoundException(userId: String) : RuntimeException("User not found: $userId")
