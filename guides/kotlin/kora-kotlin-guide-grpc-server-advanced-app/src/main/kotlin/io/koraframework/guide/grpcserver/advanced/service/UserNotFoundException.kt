package io.koraframework.guide.grpcserver.advanced.service

class UserNotFoundException(userId: String) : RuntimeException("User not found: $userId")
