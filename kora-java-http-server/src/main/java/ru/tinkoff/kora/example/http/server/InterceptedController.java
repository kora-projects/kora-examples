package ru.tinkoff.kora.example.http.server;

import java.util.concurrent.CompletionStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.tinkoff.kora.common.Component;
import ru.tinkoff.kora.common.Context;
import ru.tinkoff.kora.common.Tag;
import ru.tinkoff.kora.http.common.HttpMethod;
import ru.tinkoff.kora.http.common.annotation.HttpRoute;
import ru.tinkoff.kora.http.common.annotation.InterceptWith;
import ru.tinkoff.kora.http.common.body.HttpBody;
import ru.tinkoff.kora.http.server.common.HttpServerInterceptor;
import ru.tinkoff.kora.http.server.common.HttpServerModule;
import ru.tinkoff.kora.http.server.common.HttpServerRequest;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.annotation.HttpController;

/**
 * @see ServerInterceptor - Intercepts all controllers on HttpServer
 * @see ControllerInterceptor - Intercepts all controler methods
 * @see MethodInterceptor - Intercepts particular method
 */
@InterceptWith(InterceptedController.ControllerInterceptor.class)
@Component
@HttpController
public final class InterceptedController {

    public static final class ControllerInterceptor implements HttpServerInterceptor {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public CompletionStage<HttpServerResponse> intercept(Context context, HttpServerRequest request, InterceptChain chain)
                throws Exception {
            logger.info("Controller Level Interceptor");
            return chain.process(context, request);
        }
    }

    public static final class MethodInterceptor implements HttpServerInterceptor {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public CompletionStage<HttpServerResponse> intercept(Context context, HttpServerRequest request, InterceptChain chain)
                throws Exception {
            logger.info("Method Level Interceptor");
            return chain.process(context, request);
        }
    }

    @Tag(HttpServerModule.class)
    @Component
    public static final class ServerInterceptor implements HttpServerInterceptor {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public CompletionStage<HttpServerResponse> intercept(Context context, HttpServerRequest request, InterceptChain chain)
                throws Exception {
            logger.info("Server Level Interceptor");
            return chain.process(context, request);
        }
    }

    @InterceptWith(MethodInterceptor.class)
    @HttpRoute(method = HttpMethod.GET, path = "/intercepted")
    public HttpServerResponse get() {
        return HttpServerResponse.of(200, HttpBody.plaintext("Hello world"));
    }
}
