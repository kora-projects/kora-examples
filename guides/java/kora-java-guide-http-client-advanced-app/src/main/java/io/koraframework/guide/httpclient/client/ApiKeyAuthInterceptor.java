package io.koraframework.guide.httpclient.client;

import java.util.concurrent.CompletionStage;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.Context;
import io.koraframework.http.client.common.interceptor.HttpClientInterceptor;
import io.koraframework.http.client.common.request.HttpClientRequest;
import io.koraframework.http.client.common.response.HttpClientResponse;

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
