package ru.tinkoff.kora.guide.httpclient.client;

import java.util.concurrent.CompletionStage;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Context;
import ru.tinkoff.kora.http.client.common.interceptor.HttpClientInterceptor;
import ru.tinkoff.kora.http.client.common.request.HttpClientRequest;
import ru.tinkoff.kora.http.client.common.response.HttpClientResponse;

@Component
public final class ApiKeyAuthInterceptor implements HttpClientInterceptor {

    private final ApiKeyAuthConfig config;

    public ApiKeyAuthInterceptor(ApiKeyAuthConfig config) {
        this.config = config;
    }

    @Override
    public CompletionStage<HttpClientResponse> processRequest(Context ctx, InterceptChain chain, HttpClientRequest request)
            throws Exception {
        var authorizedRequest = request.toBuilder()
                .header("Authorization", this.config.value())
                .build();
        return chain.process(ctx, authorizedRequest);
    }
}
