package ru.tinkoff.kora.guide.grpcclient.advanced.grpc

import io.grpc.*
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.guide.grpcserver.advanced.UserStreamingServiceGrpc

@Tag(UserStreamingServiceGrpc::class)
@Component
class UserStreamingAuthInterceptor(
    private val authConfig: UserStreamingAuthConfig
) : ClientInterceptor {

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<ReqT, RespT> {
        return object :
            ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            override fun start(responseListener: Listener<RespT>, headers: Metadata) {
                headers.put(AUTHORIZATION_HEADER, authConfig.value())
                super.start(responseListener, headers)
            }
        }
    }

    companion object {
        private val AUTHORIZATION_HEADER: Metadata.Key<String> =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER)
    }
}
