package ru.tinkoff.kora.kotlin.example.grpc.client

import io.grpc.*
import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.generated.grpc.UserServiceGrpc

@Root
@Component
class RootService(private val userService: UserServiceGrpc.UserServiceBlockingStub) {
    fun service(): UserServiceGrpc.UserServiceBlockingStub = userService
}

