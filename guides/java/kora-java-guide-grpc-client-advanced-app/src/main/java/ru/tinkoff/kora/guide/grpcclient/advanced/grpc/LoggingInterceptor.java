package ru.tinkoff.kora.guide.grpcclient.advanced.grpc;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.MethodDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.guide.grpcserver.advanced.UserStreamingServiceGrpc;

@Tag(UserStreamingServiceGrpc.class)
@Component
public final class LoggingInterceptor implements ClientInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {
        logger.info("Calling gRPC method {}", method.getFullMethodName());
        return next.newCall(method, callOptions);
    }
}
