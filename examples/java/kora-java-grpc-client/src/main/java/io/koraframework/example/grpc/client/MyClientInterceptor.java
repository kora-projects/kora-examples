package io.koraframework.example.grpc.client;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Tag;
import io.koraframework.generated.grpc.UserServiceGrpc;

@Tag(UserServiceGrpc.class)
@Component
public final class MyClientInterceptor implements ClientInterceptor {

    private final Logger logger = LoggerFactory.getLogger(MyClientInterceptor.class);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT>
            interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        logger.info("INTERCEPTED");
        return next.newCall(method, callOptions);
    }
}
