package io.koraframework.guide.grpcserver.advanced.grpc

import com.google.protobuf.Timestamp
import io.koraframework.guide.grpcserver.advanced.dto.UserResponse
import java.time.ZoneOffset

internal fun UserResponse.toGrpcUser(): io.koraframework.guide.grpcserver.advanced.UserResponse {
    return io.koraframework.guide.grpcserver.advanced.UserResponse.newBuilder()
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
