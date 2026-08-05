package io.koraframework.guide.httpserver.advanced.controller;

import java.util.concurrent.CompletionStage;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.Context;
import io.koraframework.http.server.common.interceptor.HttpServerInterceptor;
import io.koraframework.http.server.common.request.HttpServerRequest;
import io.koraframework.http.server.common.response.HttpServerResponse;

@Component
public final class DataApiAuthInterceptor implements HttpServerInterceptor {

    private final DataApiAuthConfig config;

    public DataApiAuthInterceptor(DataApiAuthConfig config) {
        this.config = config;
    }

    @Override
    public CompletionStage<HttpServerResponse> intercept(Context context, HttpServerRequest request, InterceptChain chain)
            throws Exception {
        var authorization = request.headers().getFirst("authorization");
        if (!this.config.value().equals(authorization)) {
            throw new SecurityException("Invalid API key");
        }
        return chain.process(context, request);
    }
}

