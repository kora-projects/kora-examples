package io.koraframework.guide.httpserver.advanced.controller;

import io.koraframework.common.annotation.Component;
import io.koraframework.http.server.common.interceptor.HttpServerInterceptor;
import io.koraframework.http.server.common.request.HttpServerRequest;
import io.koraframework.http.server.common.response.HttpServerResponse;

@Component
public final class LoggingInterceptor implements HttpServerInterceptor {

    @Override
    public HttpServerResponse intercept(HttpServerRequest request, InterceptChain chain) throws Exception {
        long started = System.nanoTime();
        // stays 500 when the chain throws, matching the reactive version that logged 500 on failure
        int statusCode = 500;
        try {
            var response = chain.process(request);
            statusCode = response.code();
            return response;
        } finally {
            long durationMs = (System.nanoTime() - started) / 1_000_000;
            System.out.printf("Request: %s %s -> %d (%d ms)%n", request.method(), request.path(), statusCode, durationMs);
        }
    }
}
