package io.koraframework.guide.openapi.httpserver.advanced.controller

import io.koraframework.common.annotation.Component
import io.koraframework.guide.openapi.httpserver.data.model.ErrorResponseTO
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.server.common.interceptor.HttpServerInterceptor
import io.koraframework.http.server.common.request.HttpServerRequest
import io.koraframework.http.server.common.response.HttpServerResponse
import io.koraframework.http.server.common.response.HttpServerResponseException
import io.koraframework.json.common.JsonWriter
import io.koraframework.validation.common.ViolationException

@Component
class DataApiExceptionHandler(
    private val errorJsonWriter: JsonWriter<ErrorResponseTO>
) : HttpServerInterceptor {

    override fun intercept(request: HttpServerRequest, chain: HttpServerInterceptor.InterceptChain): HttpServerResponse {
        try {
            return chain.process(request)
        } catch (e: ViolationException) {
            // left to ViolationExceptionHttpServerResponseMapper, which renders the violations
            throw e
        } catch (e: HttpServerResponseException) {
            return jsonResponse(e.code(), e.message ?: "HTTP error")
        } catch (e: IllegalArgumentException) {
            return jsonResponse(400, "Invalid request parameters")
        } catch (e: SecurityException) {
            return jsonResponse(403, e.message ?: "Access denied")
        } catch (e: Exception) {
            return jsonResponse(500, "An unexpected error occurred")
        }
    }

    private fun jsonResponse(statusCode: Int, message: String): HttpServerResponse {
        return HttpServerResponse.of(
            statusCode,
            HttpBody.json(errorJsonWriter.toByteArray(ErrorResponseTO(message)))
        )
    }
}
