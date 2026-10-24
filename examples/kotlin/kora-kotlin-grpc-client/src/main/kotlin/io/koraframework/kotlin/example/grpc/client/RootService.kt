package io.koraframework.kotlin.example.grpc.client

import io.grpc.*
import org.slf4j.LoggerFactory
import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Tag
import io.koraframework.common.annotation.Root
import io.koraframework.generated.grpc.UserServiceGrpc

@Root
@Component
class RootService(private val userService: UserServiceGrpc.UserServiceBlockingStub) {
    fun service(): UserServiceGrpc.UserServiceBlockingStub = userService
}

