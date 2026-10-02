package io.koraframework.guide.httpserver.advanced.controller

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Tag
import io.koraframework.guide.httpserver.advanced.dto.ErrorResponse
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.server.common.HttpServer
import io.koraframework.http.server.common.interceptor.HttpServerInterceptor
import io.koraframework.http.server.common.request.HttpServerRequest
import io.koraframework.http.server.common.response.HttpServerResponse
import io.koraframework.http.server.common.response.HttpServerResponseException
import io.koraframework.json.common.JsonWriter

@Tag(HttpServer::class)
@Component
class ExceptionHandler(
    private val errorJsonWriter: JsonWriter<ErrorResponse>
) : HttpServerInterceptor {

    override fun intercept(request: HttpServerRequest, chain: HttpServerInterceptor.InterceptChain): HttpServerResponse {
        return try {
            chain.process(request)
        } catch (e: RestrictedFormNameException) {
            jsonResponse(400, e.message ?: "Restricted form name")
        } catch (e: HttpServerResponseException) {
            jsonResponse(e.code(), e.message ?: "HTTP error")
        } catch (e: IllegalArgumentException) {
            jsonResponse(400, "Invalid request parameters")
        } catch (e: SecurityException) {
            jsonResponse(403, e.message ?: "Access denied")
        } catch (e: Exception) {
            jsonResponse(500, "An unexpected error occurred")
        }
    }

    private fun jsonResponse(statusCode: Int, message: String): HttpServerResponse {
        return HttpServerResponse.of(
            statusCode,
            HttpBody.json(errorJsonWriter.toByteArray(ErrorResponse(message)))
        )
    }
}
