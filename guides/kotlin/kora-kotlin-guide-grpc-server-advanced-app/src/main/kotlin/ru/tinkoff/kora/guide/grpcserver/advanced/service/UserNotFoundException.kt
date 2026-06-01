package ru.tinkoff.kora.guide.grpcserver.advanced.service

class UserNotFoundException(userId: String) : RuntimeException("User not found: $userId")
