package ru.tinkoff.kora.guide.grpcclient.advanced.grpc

import io.grpc.*
import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.guide.grpcserver.advanced.UserStreamingServiceGrpc

@Tag(UserStreamingServiceGrpc::class)
@Component
class LoggingInterceptor : ClientInterceptor {

    private val logger = LoggerFactory.getLogger(LoggingInterceptor::class.java)

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<ReqT, RespT> {
        logger.info("Calling gRPC method {}", method.fullMethodName)
        return next.newCall(method, callOptions)
    }
}
