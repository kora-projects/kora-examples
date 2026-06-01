package ru.tinkoff.kora.guide.httpserver.advanced.controller;

import java.util.concurrent.CompletionStage;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Context;
import ru.tinkoff.kora.http.server.common.HttpServerInterceptor;
import ru.tinkoff.kora.http.server.common.HttpServerRequest;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;

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

