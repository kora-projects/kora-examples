package ru.tinkoff.kora.guide.grpcserver.advanced.grpc

import io.grpc.*
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.grpcserver.advanced.UserStreamingServiceGrpc

@Component
class UserStreamingAuthInterceptor(
    private val config: UserStreamingAuthConfig
) : ServerInterceptor {

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        if (UserStreamingServiceGrpc.SERVICE_NAME != call.methodDescriptor.serviceName) {
            return next.startCall(call, headers)
        }

        val authorization = headers.get(AUTHORIZATION_HEADER)
        if (config.value() != authorization) {
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid API key"), Metadata())
            return object : ServerCall.Listener<ReqT>() {}
        }
        return next.startCall(call, headers)
    }

    companion object {
        private val AUTHORIZATION_HEADER: Metadata.Key<String> =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER)
    }
}
