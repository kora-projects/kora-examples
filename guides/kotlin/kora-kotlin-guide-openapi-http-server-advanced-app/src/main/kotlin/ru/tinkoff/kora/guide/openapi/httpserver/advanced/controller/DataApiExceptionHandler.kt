package ru.tinkoff.kora.guide.openapi.httpserver.advanced.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Context
import ru.tinkoff.kora.guide.openapi.httpserver.data.model.ErrorResponseTO
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.server.common.HttpServerInterceptor
import ru.tinkoff.kora.http.server.common.HttpServerRequest
import ru.tinkoff.kora.http.server.common.HttpServerResponse
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import ru.tinkoff.kora.json.common.JsonWriter
import ru.tinkoff.kora.validation.common.ViolationException
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage

@Component
class DataApiExceptionHandler(
    private val errorJsonWriter: JsonWriter<ErrorResponseTO>
) : HttpServerInterceptor {

    override fun intercept(
        context: Context,
        request: HttpServerRequest,
        chain: HttpServerInterceptor.InterceptChain
    ): CompletionStage<HttpServerResponse> {
        return chain.process(context, request).exceptionally { throwable ->
            val cause = unwrap(throwable)
            when (cause) {
                is ViolationException -> throw CompletionException(cause)
                is HttpServerResponseException -> jsonResponse(cause.code(), cause.message ?: "HTTP error")
                is IllegalArgumentException -> jsonResponse(400, "Invalid request parameters")
                is SecurityException -> jsonResponse(403, cause.message ?: "Access denied")
                else -> jsonResponse(500, "An unexpected error occurred")
            }
        }
    }

    private fun jsonResponse(statusCode: Int, message: String): HttpServerResponse {
        return HttpServerResponse.of(
            statusCode,
            HttpBody.json(errorJsonWriter.toByteArrayUnchecked(ErrorResponseTO(message, null)))
        )
    }

    private fun unwrap(throwable: Throwable): Throwable {
        var current = throwable
        while (current is CompletionException && current.cause != null) {
            current = current.cause!!
        }
        return current
    }
}
