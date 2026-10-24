package io.koraframework.kotlin.example.grpc.client

import io.grpc.*
import org.slf4j.LoggerFactory
import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Tag
import io.koraframework.common.annotation.Root
import io.koraframework.generated.grpc.UserServiceGrpc

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

