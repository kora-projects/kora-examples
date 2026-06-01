package ru.tinkoff.kora.guide.grpcserver.advanced.grpc

import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component

@Component
class LoggingInterceptor : ServerInterceptor {

    private val logger = LoggerFactory.getLogger(LoggingInterceptor::class.java)

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        logger.info("Incoming gRPC request: method={}", call.methodDescriptor.fullMethodName)
        return next.startCall(call, headers)
    }
}
