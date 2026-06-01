package ru.tinkoff.kora.guide.httpserver.advanced.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Context
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.guide.httpserver.advanced.dto.ErrorResponse
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.server.common.*
import ru.tinkoff.kora.json.common.JsonWriter
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage

@Tag(HttpServerModule::class)
@Component
class ExceptionHandler(
    private val errorJsonWriter: JsonWriter<ErrorResponse>
) : HttpServerInterceptor {

    override fun intercept(
        context: Context,
        request: HttpServerRequest,
        chain: HttpServerInterceptor.InterceptChain
    ): CompletionStage<HttpServerResponse> {
        return chain.process(context, request).exceptionally { throwable ->
            val cause = unwrap(throwable)
            when (cause) {
                is RestrictedFormNameException -> jsonResponse(400, cause.message ?: "Restricted form name")
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
            HttpBody.json(errorJsonWriter.toByteArrayUnchecked(ErrorResponse(message)))
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
