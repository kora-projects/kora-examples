package ru.tinkoff.kora.guide.grpcserver.advanced.grpc;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.guide.grpcserver.advanced.UserStreamingServiceGrpc;

@Component
public final class UserStreamingAuthInterceptor implements ServerInterceptor {

    private static final Metadata.Key<String> AUTHORIZATION_HEADER =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final UserStreamingAuthConfig config;

    public UserStreamingAuthInterceptor(UserStreamingAuthConfig config) {
        this.config = config;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        if (!UserStreamingServiceGrpc.SERVICE_NAME.equals(call.getMethodDescriptor().getServiceName())) {
            return next.startCall(call, headers);
        }

        var authorization = headers.get(AUTHORIZATION_HEADER);
        if (!this.config.value().equals(authorization)) {
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid API key"), new Metadata());
            return new ServerCall.Listener<>() {};
        }

        return next.startCall(call, headers);
    }
}
