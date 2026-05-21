package ru.tinkoff.kora.kotlin.example.http.server

import jakarta.annotation.Nullable
import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Context
import ru.tinkoff.kora.common.Mapping
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.HttpResponseEntity
import ru.tinkoff.kora.http.common.annotation.*
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.common.form.FormMultipart
import ru.tinkoff.kora.http.common.header.HttpHeaders
import ru.tinkoff.kora.http.server.common.*
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.http.server.common.handler.HttpServerRequestMapper
import ru.tinkoff.kora.http.server.common.handler.HttpServerResponseMapper
import ru.tinkoff.kora.json.common.JsonWriter
import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.validation.common.annotation.Pattern
import ru.tinkoff.kora.validation.common.annotation.Size
import ru.tinkoff.kora.validation.common.annotation.Validate
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

    @Tag(HttpServerModule::class)
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

