package io.koraframework.example.http.client;

import java.util.concurrent.CompletionStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.koraframework.common.Context;
import io.koraframework.http.client.common.annotation.HttpClient;
import io.koraframework.http.client.common.interceptor.HttpClientInterceptor;
import io.koraframework.http.client.common.request.HttpClientRequest;
import io.koraframework.http.client.common.response.HttpClientResponse;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.HttpResponseEntity;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.annotation.InterceptWith;

@InterceptWith(InterceptedHttpClient.ClientInterceptor.class)
@HttpClient(configPath = "httpClient.default")
public interface InterceptedHttpClient {

    final class ClientInterceptor implements HttpClientInterceptor {

        private static final Logger logger = LoggerFactory.getLogger(ClientInterceptor.class);

        @Override
        public CompletionStage<HttpClientResponse> processRequest(Context ctx, InterceptChain chain, HttpClientRequest request)
                throws Exception {
            logger.info("Client Level Interceptor");
            return chain.process(ctx, request);
        }
    }

    final class MethodInterceptor implements HttpClientInterceptor {

        private static final Logger logger = LoggerFactory.getLogger(MethodInterceptor.class);

        @Override
        public CompletionStage<HttpClientResponse> processRequest(Context ctx, InterceptChain chain, HttpClientRequest request)
                throws Exception {
            logger.info("Method Level Interceptor");
            return chain.process(ctx, request);
        }
    }

    @InterceptWith(MethodInterceptor.class)
    @HttpRoute(method = HttpMethod.GET, path = "/intercepted")
    HttpResponseEntity<String> get();
}
