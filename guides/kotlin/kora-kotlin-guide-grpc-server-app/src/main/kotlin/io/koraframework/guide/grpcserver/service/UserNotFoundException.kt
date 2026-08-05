package io.koraframework.guide.grpcserver.service

class UserNotFoundException(userId: String) : RuntimeException("User not found: $userId")
