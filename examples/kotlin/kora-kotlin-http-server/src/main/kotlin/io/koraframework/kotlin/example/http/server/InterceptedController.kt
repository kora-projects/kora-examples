package io.koraframework.kotlin.example.http.server

import org.slf4j.LoggerFactory
import io.koraframework.common.annotation.Component
import io.koraframework.common.Context
import io.koraframework.common.annotation.Mapping
import io.koraframework.common.annotation.Tag
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.HttpResponseEntity
import io.koraframework.http.common.annotation.*
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.common.form.FormMultipart
import io.koraframework.http.common.header.HttpHeaders
import io.koraframework.http.server.common.*
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.http.server.common.request.HttpServerRequestMapper
import io.koraframework.http.server.common.response.HttpServerResponseMapper
import io.koraframework.json.common.JsonWriter
import io.koraframework.json.common.annotation.Json
import io.koraframework.validation.common.annotation.Pattern
import io.koraframework.validation.common.annotation.Size
import io.koraframework.validation.common.annotation.Validate
import java.util.concurrent.CompletionStage

@InterceptWith(InterceptedController.ControllerInterceptor::class)
@Component
@HttpController
class InterceptedController {
    class ControllerInterceptor : HttpServerInterceptor {
        private val logger = LoggerFactory.getLogger(ControllerInterceptor::class.java)

        override fun intercept(
            ctx: Context,
            request: HttpServerRequest,
            chain: HttpServerInterceptor.InterceptChain
        ): CompletionStage<HttpServerResponse> {
            logger.info("Controller Level Interceptor")
            return chain.process(ctx, request)
        }
    }

    class MethodInterceptor : HttpServerInterceptor {
        private val logger = LoggerFactory.getLogger(MethodInterceptor::class.java)

        override fun intercept(
            ctx: Context,
            request: HttpServerRequest,
            chain: HttpServerInterceptor.InterceptChain
        ): CompletionStage<HttpServerResponse> {
            logger.info("Method Level Interceptor")
            return chain.process(ctx, request)
        }
    }

    @Tag(HttpServer::class)
    @Component
    class ServerInterceptor : HttpServerInterceptor {
        private val logger = LoggerFactory.getLogger(ServerInterceptor::class.java)

        override fun intercept(
            ctx: Context,
            request: HttpServerRequest,
            chain: HttpServerInterceptor.InterceptChain
        ): CompletionStage<HttpServerResponse> {
            logger.info("Server Level Interceptor")
            return chain.process(ctx, request)
        }
    }

    @InterceptWith(MethodInterceptor::class)
    @HttpRoute(method = HttpMethod.GET, path = "/intercepted")
    fun get(): HttpServerResponse = HttpServerResponse.of(200, HttpBody.plaintext("Hello world"))
}

