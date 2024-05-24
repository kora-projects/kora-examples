package ru.tinkoff.kora.example.grpc.client;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.generated.grpc.UserServiceGrpc;

@Tag(UserServiceGrpc.class)
@Component
public final class MyClientInterceptor implements ClientInterceptor {

    private final Logger logger = LoggerFactory.getLogger(MyClientInterceptor.class);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        logger.info("INTERCEPTED");
        return next.newCall(method, callOptions);
    }
}
