package ru.tinkoff.kora.guide.grpcserver.advanced.grpc

import com.google.protobuf.Timestamp
import ru.tinkoff.kora.guide.grpcserver.advanced.dto.UserResponse
import java.time.ZoneOffset

internal fun UserResponse.toGrpcUser(): ru.tinkoff.kora.guide.grpcserver.advanced.UserResponse {
    return ru.tinkoff.kora.guide.grpcserver.advanced.UserResponse.newBuilder()
        .setId(id)
        .setName(name)
        .setEmail(email)
        .setCreatedAt(
            Timestamp.newBuilder()
                .setSeconds(createdAt.toEpochSecond(ZoneOffset.UTC))
                .setNanos(createdAt.nano)
                .build()
        )
        .build()
}
