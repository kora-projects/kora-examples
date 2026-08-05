package io.koraframework.guide.openapi.httpserver.advanced.controller

import io.koraframework.common.annotation.Component
import io.koraframework.common.Context
import io.koraframework.guide.openapi.httpserver.data.model.ErrorResponseTO
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.server.common.interceptor.HttpServerInterceptor
import io.koraframework.http.server.common.request.HttpServerRequest
import io.koraframework.http.server.common.response.HttpServerResponse
import io.koraframework.http.server.common.response.HttpServerResponseException
import io.koraframework.json.common.JsonWriter
import io.koraframework.validation.common.ViolationException
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
