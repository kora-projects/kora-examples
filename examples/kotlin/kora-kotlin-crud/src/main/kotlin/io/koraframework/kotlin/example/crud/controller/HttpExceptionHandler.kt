package io.koraframework.kotlin.example.crud.controller

import io.micrometer.core.instrument.config.validate.ValidationException
import org.slf4j.LoggerFactory
import io.koraframework.common.annotation.Component
import io.koraframework.common.Context
import io.koraframework.common.annotation.Tag
import io.koraframework.example.crud.openapi.http.server.model.MessageTO
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.server.common.*
import io.koraframework.json.common.JsonWriter
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeoutException

@Tag(HttpServer::class)
@Component
class HttpExceptionHandler(private val errorJsonWriter: JsonWriter<MessageTO>) : HttpServerInterceptor {

    companion object {
        val logger = LoggerFactory.getLogger(HttpExceptionHandler::class.java)!!
    }

    override fun intercept(
        context: Context,
        request: HttpServerRequest,
        chain: HttpServerInterceptor.InterceptChain
    ): CompletionStage<HttpServerResponse> {
        return chain.process(context, request).exceptionally { e ->
            if (e is HttpServerResponseException) {
                return@exceptionally e
            }

            val body = HttpBody.json(errorJsonWriter.toByteArrayUnchecked(MessageTO(e.message)))
            when (e) {
                is ValidationException -> HttpServerResponse.of(400, body)
                is IllegalArgumentException -> HttpServerResponse.of(400, body)
                is TimeoutException -> HttpServerResponse.of(408, body)
                else -> {
                    logger.error("Request '{} {}' failed", request.method(), request.path(), e)
                    HttpServerResponse.of(500, body)
                }
            }
        }
    }
}
