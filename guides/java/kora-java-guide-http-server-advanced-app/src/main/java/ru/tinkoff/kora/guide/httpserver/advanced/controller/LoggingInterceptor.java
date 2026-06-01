package ru.tinkoff.kora.guide.httpserver.advanced.controller;

import java.util.concurrent.CompletionStage;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Context;
import ru.tinkoff.kora.http.server.common.HttpServerInterceptor;
import ru.tinkoff.kora.http.server.common.HttpServerRequest;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;

@Component
public final class LoggingInterceptor implements HttpServerInterceptor {

    @Override
    public CompletionStage<HttpServerResponse> intercept(Context context, HttpServerRequest request, InterceptChain chain)
            throws Exception {
        long started = System.nanoTime();
        return chain.process(context, request).whenComplete((response, throwable) -> {
            long durationMs = (System.nanoTime() - started) / 1_000_000;
            int statusCode = response != null ? response.code() : 500;
            System.out.printf("Request: %s %s -> %d (%d ms)%n", request.method(), request.path(), statusCode, durationMs);
        });
    }
}
