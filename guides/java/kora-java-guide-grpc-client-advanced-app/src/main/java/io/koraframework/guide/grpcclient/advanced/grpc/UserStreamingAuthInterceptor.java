package io.koraframework.guide.grpcclient.advanced.grpc;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Tag;
import io.koraframework.guide.grpcserver.advanced.UserStreamingServiceGrpc;

@Tag(UserStreamingServiceGrpc.class)
@Component
public final class UserStreamingAuthInterceptor implements ClientInterceptor {

    private static final Metadata.Key<String> AUTHORIZATION_HEADER =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final UserStreamingAuthConfig authConfig;

    public UserStreamingAuthInterceptor(UserStreamingAuthConfig authConfig) {
        this.authConfig = authConfig;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(AUTHORIZATION_HEADER, authConfig.value());
                super.start(responseListener, headers);
            }
        };
    }
}
