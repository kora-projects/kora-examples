package io.koraframework.example.http.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.koraframework.common.annotation.Component;
import io.koraframework.common.annotation.Tag;
import io.koraframework.http.common.HttpMethod;
import io.koraframework.http.common.annotation.HttpRoute;
import io.koraframework.http.common.annotation.InterceptWith;
import io.koraframework.http.common.body.HttpBody;
import io.koraframework.http.server.common.interceptor.HttpServerInterceptor;
import io.koraframework.http.server.common.HttpServer;
import io.koraframework.http.server.common.request.HttpServerRequest;
import io.koraframework.http.server.common.response.HttpServerResponse;
import io.koraframework.http.server.common.annotation.HttpController;

/**
 * @see ServerInterceptor - Intercepts all controllers on HttpServer
 * @see ControllerInterceptor - Intercepts all controler methods
 * @see MethodInterceptor - Intercepts particular method
 */
@InterceptWith(InterceptedController.ControllerInterceptor.class)
@Component
@HttpController
public final class InterceptedController {

    @Component
    public static final class ControllerInterceptor implements HttpServerInterceptor {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public HttpServerResponse intercept(HttpServerRequest request, InterceptChain chain) throws Exception {
            logger.info("Controller Level Interceptor");
            return chain.process(request);
        }
    }

    @Component
    public static final class MethodInterceptor implements HttpServerInterceptor {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public HttpServerResponse intercept(HttpServerRequest request, InterceptChain chain) throws Exception {
            logger.info("Method Level Interceptor");
            return chain.process(request);
        }
    }

    @Tag(HttpServer.class)
    @Component
    public static final class ServerInterceptor implements HttpServerInterceptor {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public HttpServerResponse intercept(HttpServerRequest request, InterceptChain chain) throws Exception {
            logger.info("Server Level Interceptor");
            return chain.process(request);
        }
    }

    @InterceptWith(MethodInterceptor.class)
    @HttpRoute(method = HttpMethod.GET, path = "/intercepted")
    public HttpServerResponse get() {
        return HttpServerResponse.of(200, HttpBody.plaintext("Hello world"));
    }
}
