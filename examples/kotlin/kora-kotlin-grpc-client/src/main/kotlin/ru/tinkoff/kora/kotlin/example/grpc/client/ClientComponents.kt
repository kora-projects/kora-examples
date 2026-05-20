package ru.tinkoff.kora.kotlin.example.grpc.client

import io.grpc.*
import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.generated.grpc.UserServiceGrpc

@Tag(UserServiceGrpc::class)
@Component
class MyClientInterceptor : ClientInterceptor {
    private val logger = LoggerFactory.getLogger(MyClientInterceptor::class.java)

    override fun <ReqT : Any, RespT : Any> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<ReqT, RespT> {
        logger.info("INTERCEPTED")
        return next.newCall(method, callOptions)
    }
}

@Root
@Component
class RootService(private val userService: UserServiceGrpc.UserServiceBlockingStub) {
    fun service(): UserServiceGrpc.UserServiceBlockingStub = userService
}
