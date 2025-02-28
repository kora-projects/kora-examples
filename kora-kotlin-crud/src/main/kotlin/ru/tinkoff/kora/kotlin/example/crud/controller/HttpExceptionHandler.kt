package ru.tinkoff.kora.kotlin.example.crud.controller

import io.micrometer.core.instrument.config.validate.ValidationException
import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Context
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.example.crud.openapi.http.server.model.MessageTO
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.server.common.*
import ru.tinkoff.kora.json.common.JsonWriter
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeoutException

@Tag(HttpServerModule::class)
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
            val error = if (e is CompletionException) e.cause!! else e
            if (error is HttpServerResponseException) {
                return@exceptionally error
            }

            val body = HttpBody.json(errorJsonWriter.toByteArrayUnchecked(MessageTO(error.message)))
            when (error) {
                is ValidationException -> HttpServerResponse.of(400, body)
                is IllegalArgumentException -> HttpServerResponse.of(400, body)
                is TimeoutException -> HttpServerResponse.of(408, body)
                else -> {
                    logger.error("Request '{} {}' failed", request.method(), request.path(), error)
                    HttpServerResponse.of(500, body)
                }
            }
        }
    }
}



