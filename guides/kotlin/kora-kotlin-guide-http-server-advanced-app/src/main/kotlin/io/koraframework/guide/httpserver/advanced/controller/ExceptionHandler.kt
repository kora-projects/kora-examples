package io.koraframework.guide.httpserver.advanced.controller

import io.koraframework.common.annotation.Component
import io.koraframework.common.Context
import io.koraframework.common.annotation.Tag
import io.koraframework.guide.httpserver.advanced.dto.ErrorResponse
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.server.common.*
import io.koraframework.json.common.JsonWriter
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage

@Tag(HttpServer::class)
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
            HttpBody.json(errorJsonWriter.toByteArray(ErrorResponse(message)))
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
