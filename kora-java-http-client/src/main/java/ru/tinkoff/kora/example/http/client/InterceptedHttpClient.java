package ru.tinkoff.kora.example.http.client;

import java.util.concurrent.CompletionStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Context;
import ru.tinkoff.kora.http.client.common.annotation.HttpClient;
import ru.tinkoff.kora.http.client.common.interceptor.HttpClientInterceptor;
import ru.tinkoff.kora.http.client.common.request.HttpClientRequest;
import ru.tinkoff.kora.http.client.common.response.HttpClientResponse;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.HttpResponseEntity;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.annotation.InterceptWith;

@InterceptWith(InterceptedHttpClient.ClientInterceptor.class)
@Component
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
