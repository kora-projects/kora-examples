package io.koraframework.kotlin.example.http.server

import org.slf4j.LoggerFactory
import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Tag
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.annotation.InterceptWith
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.server.common.HttpServer
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.http.server.common.interceptor.HttpServerInterceptor
import io.koraframework.http.server.common.request.HttpServerRequest
import io.koraframework.http.server.common.response.HttpServerResponse

@InterceptWith(InterceptedController.ControllerInterceptor::class)
@Component
@HttpController
class InterceptedController {

    @Component
    class ControllerInterceptor : HttpServerInterceptor {
        private val logger = LoggerFactory.getLogger(ControllerInterceptor::class.java)

        override fun intercept(request: HttpServerRequest, chain: HttpServerInterceptor.InterceptChain): HttpServerResponse {
            logger.info("Controller Level Interceptor")
            return chain.process(request)
        }
    }

    @Component
    class MethodInterceptor : HttpServerInterceptor {
        private val logger = LoggerFactory.getLogger(MethodInterceptor::class.java)

        override fun intercept(request: HttpServerRequest, chain: HttpServerInterceptor.InterceptChain): HttpServerResponse {
            logger.info("Method Level Interceptor")
            return chain.process(request)
        }
    }

    @Tag(HttpServer::class)
    @Component
    class ServerInterceptor : HttpServerInterceptor {
        private val logger = LoggerFactory.getLogger(ServerInterceptor::class.java)

        override fun intercept(request: HttpServerRequest, chain: HttpServerInterceptor.InterceptChain): HttpServerResponse {
            logger.info("Server Level Interceptor")
            return chain.process(request)
        }
    }

    @InterceptWith(MethodInterceptor::class)
    @HttpRoute(method = HttpMethod.GET, path = "/intercepted")
    fun get(): HttpServerResponse = HttpServerResponse.of(200, HttpBody.plaintext("Hello world"))
}
