package io.koraframework.kotlin.example.submodule.app.controller

import io.micrometer.core.instrument.config.validate.ValidationException
import org.slf4j.LoggerFactory
import io.koraframework.common.annotation.Component
import io.koraframework.common.Context
import io.koraframework.common.annotation.Tag
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.server.common.*
import io.koraframework.json.common.JsonWriter
import io.koraframework.kotlin.example.submodule.openapi.http.server.model.MessageTO
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeoutException

@Tag(HttpServerModule::class)
@Component
class HttpExceptionHandler(private val errorJsonWriter: JsonWriter<MessageTO>) : HttpServerInterceptor {
    override fun intercept(
        context: Context,
        request: HttpServerRequest,
        chain: HttpServerInterceptor.InterceptChain,
    ): CompletionStage<HttpServerResponse> {
        return chain.process(context, request).exceptionally { e ->
            if (e is HttpServerResponseException) {
                return@exceptionally e
            }

            val body = HttpBody.json(errorJsonWriter.toByteArrayUnchecked(MessageTO(e.message)))
            when (e) {
                is ValidationException, is IllegalArgumentException -> HttpServerResponse.of(400, body)
                is TimeoutException -> HttpServerResponse.of(408, body)
                else -> {
                    logger.error("Request '{} {}' failed", request.method(), request.path(), e)
                    HttpServerResponse.of(500, body)
                }
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HttpExceptionHandler::class.java)
    }
}
